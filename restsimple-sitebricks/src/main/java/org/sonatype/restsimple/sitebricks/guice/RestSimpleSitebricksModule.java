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
package org.sonatype.restsimple.sitebricks.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;
import com.google.sitebricks.At;
import com.google.sitebricks.Show;
import com.google.sitebricks.rendering.EmbedAs;
import com.google.sitebricks.rendering.With;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.restsimple.annotation.Service;
import org.sonatype.restsimple.api.DefaultServiceDefinition;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.sitebricks.impl.SitebricksServiceDefinitionGenerator;
import org.sonatype.restsimple.spi.NegotiationTokenGenerator;
import org.sonatype.restsimple.spi.RFC2295NegotiationTokenGenerator;
import org.sonatype.restsimple.spi.ResourceModuleConfig;
import org.sonatype.restsimple.spi.ServiceDefinitionModule;
import org.sonatype.restsimple.spi.ServiceDefinitionGenerator;
import org.sonatype.restsimple.spi.ServiceHandlerMapper;
import org.sonatype.restsimple.spi.scan.Classes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.inject.matcher.Matchers.annotatedWith;

/**
 * Base class for deploying {@link org.sonatype.restsimple.api.ServiceDefinition} to a Sitebricks.
 */
public class RestSimpleSitebricksModule
    extends ServletModule implements ServiceDefinitionModule {

    private final Logger logger = LoggerFactory.getLogger(RestSimpleSitebricksModule.class);

    private Injector parent;
    private Injector injector;
    private final List<Package> packages = new ArrayList<Package>();
    private final Set<Class<?>> classesSet = new HashSet<Class<?>>();
    private final Set<ServiceDefinition> sdSet = new HashSet<ServiceDefinition>();

    public RestSimpleSitebricksModule() {
        this(null, null);
    }

    public RestSimpleSitebricksModule( Injector parent ) {
        this(parent, null);
    }

    public RestSimpleSitebricksModule( Injector parent, ServiceHandlerMapper mapper ) {
        this.parent = parent;
    }

    public RestSimpleSitebricksModule( ServiceHandlerMapper mapper ) {
        this(null, mapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void configureServlets() {

        boolean bindServiceDefinition = true;
        if (parent == null) {
            injector = Guice.createInjector();
        } else {
            injector = parent;
        }

        sdSet.addAll( defineServices( injector ) );
        ServiceDefinitionGenerator generator = new SitebricksServiceDefinitionGenerator( new ResourceModuleConfig<Module>(){

            @Override
            public <A> void bindToInstance(Class<A> clazz, A instance) {
                binder().bind(clazz).toInstance(instance);
            }

            @Override
            public <A> void bindTo(Class<A> clazz, Class<? extends A> clazz2) {
                binder().bind(clazz).to(clazz2);
            }

            @Override
            public void bind(Class<?> clazz) {
                binder().bind(clazz);
            }

            @Override
            public void install(Module module) {
                binder().install(module);
            }
        });

        NegotiationTokenGenerator token = injector.getInstance( NegotiationTokenGenerator.class );
        bind(NegotiationTokenGenerator.class).toInstance( token );
        
        ServiceHandlerMapper mapper = injector.getInstance(ServiceHandlerMapper.class);
        bind( ServiceHandlerMapper.class ).toInstance( mapper );

        if (sdSet != null && sdSet.size() > 0) {
            for (ServiceDefinition sd : sdSet) {
                generator.generate(sd, mapper);
            }
        }

        classesSet.clear();
        for (Package pkg : packages) {

            //look for any classes annotated with @At, @EmbedAs and @With
            classesSet.addAll(Classes.matching(
                            annotatedWith(At.class).or(
                            annotatedWith(EmbedAs.class)).or(
                            annotatedWith(With.class)).or(
                            annotatedWith(Show.class))
            ).in(pkg));
        }

        ServiceDefinition sd;
        for (Class<?> clazz: classesSet) {
            sd = new DefaultServiceDefinition().extendWith(clazz);
            generator.generate(sd, mapper);
        }
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
    public NegotiationTokenGenerator configureNegotiationTokenGenerator(){
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
    public RestSimpleSitebricksModule scan(Package packageName) {
        packages.add(packageName);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RestSimpleSitebricksModule addClass( Class<?> className ) {
        classesSet.add( className );
        return this;
    }

    @Override
    public RestSimpleSitebricksModule addInstance( ServiceDefinition instance ) {
        sdSet.add( instance );
        return this;
    }
}
