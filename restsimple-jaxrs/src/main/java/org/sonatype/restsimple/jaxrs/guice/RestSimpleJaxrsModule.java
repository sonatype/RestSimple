/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.restsimple.jaxrs.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.restsimple.annotation.Service;
import org.sonatype.restsimple.api.DefaultServiceDefinition;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.jaxrs.impl.ContentNegotiationFilter;
import org.sonatype.restsimple.jaxrs.impl.JAXRSServiceDefinitionGenerator;
import org.sonatype.restsimple.spi.NegotiationTokenGenerator;
import org.sonatype.restsimple.spi.RFC2295NegotiationTokenGenerator;
import org.sonatype.restsimple.spi.ResourceModuleConfig;
import org.sonatype.restsimple.spi.ServiceDefinitionConfig;
import org.sonatype.restsimple.spi.ServiceDefinitionGenerator;
import org.sonatype.restsimple.spi.ServiceHandlerMapper;
import org.sonatype.restsimple.spi.scan.Classes;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.inject.matcher.Matchers.annotatedWith;

/**
 * Base class for deploying {@link ServiceDefinition} to a JAXRS implementation.
 */
public class RestSimpleJaxrsModule
    extends ServletModule implements ServiceDefinitionConfig {
    private final Logger logger = LoggerFactory.getLogger(RestSimpleJaxrsModule.class);

    private Injector parent;
    private Injector injector;
    private final Map<String,String> jaxrsProperties;
    private final List<Package> packages = new ArrayList<Package>();
    private final String filterPath;
    private final String servletPath;
    private final Set<Class<?>> classesSet = new HashSet<Class<?>>();
    private final Set<ServiceDefinition> sdSet = new HashSet<ServiceDefinition>();

    public RestSimpleJaxrsModule() {
        this(null, new HashMap<String,String>());
    }

    public RestSimpleJaxrsModule( Map<String, String> jaxrsProperties ) {
        this(null, jaxrsProperties);
    }

    public RestSimpleJaxrsModule( Injector parent ) {
        this(parent, new HashMap<String,String>());
    }

    public RestSimpleJaxrsModule( Injector parent, Map<String, String> jaxrsProperties ) {
        this( parent, jaxrsProperties, "/*", "/*" );
    }

    public RestSimpleJaxrsModule( Injector parent, Map<String, String> jaxrsProperties, String filterPath,
                                  String servletPath ) {

        this.jaxrsProperties = jaxrsProperties;
        this.parent = parent;
        this.filterPath = filterPath;
        this.servletPath = servletPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void configureServlets() {
        if (parent == null) {
            injector = Guice.createInjector();
        } else if (parent.getBinding( ServiceDefinition.class ) == null) {
            injector = parent.createChildInjector();
        } else {
            injector = parent;
        }

        NegotiationTokenGenerator token = injector.getInstance( NegotiationTokenGenerator.class );
        if (parent == null || parent.getBinding(NegotiationTokenGenerator.class ) == null) {
            bind(NegotiationTokenGenerator.class).toInstance( token );
        }

        ServiceHandlerMapper mapper = injector().getInstance(  ServiceHandlerMapper.class );
        if (parent == null || parent.getBinding(ServiceHandlerMapper.class ) == null) {
            bind( ServiceHandlerMapper.class ).toInstance( mapper );
        }

        sdSet.addAll( defineServices( injector ) );
        ServiceDefinitionGenerator generator = new JAXRSServiceDefinitionGenerator( new ResourceModuleConfig<Module>() {

            @Override
            public <A> void bindToInstance(Class<A> clazz, A instance) {
                binder().withSource( "[generated]" ).bind( clazz ).toInstance(instance);
            }

            @Override
            public <A> void bindTo(Class<A> clazz, Class<? extends A> clazz2) {
                binder().withSource( "[generated]" ).bind( clazz ).to(clazz2);
            }

            @Override
            public void bind(Class<?> clazz) {
                binder().withSource( "[generated]" ).bind( clazz ).asEagerSingleton();
            }

            @Override
            public void install(Module module) {
            }
        });

        if (sdSet != null && sdSet.size() > 0) {
            for (ServiceDefinition sd : sdSet) {
                generator.generate(sd, mapper);
            }
        }

        classesSet.clear();
        for (Package pkg : packages) {
            //look for any classes annotated with @Path, @PathParam
            classesSet.addAll(Classes.matching(
                    annotatedWith(Path.class).or(
                            annotatedWith(PathParam.class))
            ).in(pkg));
        }

        for (Class<?> clazz: classesSet) {
            generator.generate(new DefaultServiceDefinition().extendWith(clazz), mapper);
        }

        jaxrsProperties.put("com.sun.jersey.api.json.POJOMappingFeature", "true");
        //jaxrsProperties.put("com.sun.jersey.config.feature.Trace", "true");

        filter(filterPath).through(ContentNegotiationFilter.class);
        serve(servletPath).with(GuiceContainer.class, jaxrsProperties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceDefinition> defineServices(Injector injector) {

        for (Package pkg : packages) {
            //look for any classes annotated with @Service
            classesSet.addAll(Classes.matching(
                    annotatedWith(Service.class)
            ).in(pkg));
        }

        List<ServiceDefinition> list = new ArrayList<ServiceDefinition>();
        // Now let's find the method that returns a ServiceDefinition
        for(Class<?> clazz : classesSet) {

            Object o = injector.getInstance(clazz);
            Method[] methods = clazz.getMethods();
            for(Method method: methods) {
                Class<?> returnType = method.getReturnType();
                ServiceDefinition sd;
                if (returnType.equals(ServiceDefinition.class)) {
                    try {
                        sd  = ServiceDefinition.class.cast(method.invoke(o, null));
                        list.add(sd);
                    } catch (IllegalAccessException e) {
                        logger.trace("defineServices",e);
                    } catch (InvocationTargetException e) {
                        logger.trace("defineServices",e);
                    }
                }
            }
        }
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NegotiationTokenGenerator configureNegotiationTokenGenerator() {
        return new RFC2295NegotiationTokenGenerator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Injector injector() {
        return injector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RestSimpleJaxrsModule scan(Package packageName) {
        packages.add(packageName);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RestSimpleJaxrsModule addClass( Class<?> className ) {
        classesSet.add( className );
        return this;
    }

    @Override
    public RestSimpleJaxrsModule addInstance( ServiceDefinition instance ){
        sdSet.add( instance );
        return null;
    }

}
