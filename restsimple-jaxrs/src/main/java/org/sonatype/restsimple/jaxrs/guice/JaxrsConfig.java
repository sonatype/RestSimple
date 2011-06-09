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
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.sonatype.restsimple.api.DefaultServiceDefinition;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.jaxrs.impl.ContentNegotiationFilter;
import org.sonatype.restsimple.jaxrs.impl.JAXRSServiceDefinitionGenerator;
import org.sonatype.restsimple.spi.NegotiationTokenGenerator;
import org.sonatype.restsimple.spi.RFC2295NegotiationTokenGenerator;
import org.sonatype.restsimple.spi.ServiceDefinitionConfig;
import org.sonatype.restsimple.spi.ServiceDefinitionGenerator;
import org.sonatype.restsimple.spi.ServiceHandlerMapper;
import org.sonatype.restsimple.spi.scan.Classes;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
public class JaxrsConfig extends ServletModule implements ServiceDefinitionConfig {

    private Injector parent;
    private Injector injector;
    private final Map<String,String> jaxrsProperties;
    private final ServiceHandlerMapper mapper;
    private final List<Package> packages = new ArrayList<Package>();
    private final String filterPath;
    private final String servletPath;

    public JaxrsConfig() {
        this(null, new HashMap<String,String>());
    }

    public JaxrsConfig(ServiceHandlerMapper mapper) {
        this(null, new HashMap<String,String>(), mapper, "/*", "/*");
    }

    public JaxrsConfig(Map<String,String> jaxrsProperties) {
        this(null, jaxrsProperties);
    }

    public JaxrsConfig(Map<String,String> jaxrsProperties, ServiceHandlerMapper mapper) {
        this(null, jaxrsProperties, mapper, "/*", "/*");
    }

    public JaxrsConfig(Injector parent) {
        this(parent, new HashMap<String,String>());
    }

    public JaxrsConfig(Injector parent, ServiceHandlerMapper mapper) {
        this(parent, new HashMap<String,String>(), mapper, "/*", "/*");
    }

    public JaxrsConfig(Injector parent, Map<String,String> jaxrsProperties) {
        this(parent, jaxrsProperties, new ServiceHandlerMapper(), "/*", "/*");
    }

    public JaxrsConfig(Injector parent,
                       Map<String,String> jaxrsProperties,
                       ServiceHandlerMapper mapper,
                       String filterPath,
                       String servletPath) {
        this.jaxrsProperties = jaxrsProperties;
        this.parent = parent;
        this.mapper = mapper;
        this.filterPath = filterPath;
        this.servletPath = servletPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void configureServlets() {
        injector = Guice.createInjector(new JaxrsModule(binder().withSource("[generated]"), mapper));
        List<ServiceDefinition> list = defineServices(parent != null ? parent : injector);
        ServiceDefinitionGenerator generator = injector.getInstance(JAXRSServiceDefinitionGenerator.class);

        if (list != null && list.size() > 0) {
            for (ServiceDefinition sd : list) {
                generator.generate(sd);
            }
        }

        Set<Class<?>> set = new HashSet<Class<?>>();
        for (Package pkg : packages) {
            //look for any classes annotated with @Path, @PathParam
            set.addAll(Classes.matching(
                    annotatedWith(Path.class).or(
                            annotatedWith(PathParam.class))
            ).in(pkg));
        }

        for (Class<?> clazz: set) {
            generator.generate(new DefaultServiceDefinition().extendWith(clazz));
        }

        jaxrsProperties.put("com.sun.jersey.api.json.POJOMappingFeature", "true");
        //jaxrsProperties.put("com.sun.jersey.config.feature.Trace", "true");

        filter("/*").through(ContentNegotiationFilter.class);
        serve("/*").with(GuiceContainer.class, jaxrsProperties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceDefinition> defineServices(Injector injector) {
        return null;
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
    public void scan(Package packageName) {
        packages.add(packageName);
    }

}
