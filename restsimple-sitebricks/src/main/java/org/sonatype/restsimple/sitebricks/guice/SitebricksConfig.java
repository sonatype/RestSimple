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
import org.sonatype.restsimple.spi.ServiceDefinitionConfig;
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
public class SitebricksConfig extends ServletModule implements ServiceDefinitionConfig {

    private final Logger logger = LoggerFactory.getLogger(SitebricksConfig.class);

    private Injector parent;
    private Injector injector;
    private final ServiceHandlerMapper mapper;
    private final boolean createChild;
    private final List<Package> packages = new ArrayList<Package>();

    public SitebricksConfig() {
        this(null, new ServiceHandlerMapper(), false);
    }

    public SitebricksConfig(Injector parent) {
        this(parent, new ServiceHandlerMapper(), false);
    }

    public SitebricksConfig(Injector parent, ServiceHandlerMapper mapper) {
        this(parent, mapper, false);
    }

    public SitebricksConfig(Injector parent, ServiceHandlerMapper mapper, boolean createChild) {
        this.parent = parent;
        this.mapper = mapper;
        this.createChild = createChild;
    }

    public SitebricksConfig(ServiceHandlerMapper mapper) {
        this(null, mapper, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void configureServlets() {
        if (!createChild || parent == null) {
            injector = Guice.createInjector(new RestSimpleSitebricksModule(binder(), mapper));
        } else {
            injector = parent.createChildInjector( new RestSimpleSitebricksModule(binder(), mapper, createChild) );
        }

        List<ServiceDefinition> list = defineServices( parent == null ? injector : parent);
        ServiceDefinitionGenerator generator = injector.getInstance(SitebricksServiceDefinitionGenerator.class);
        if (list != null && list.size() > 0) {
            for (ServiceDefinition sd : list) {
                generator.generate(sd);
            }
        }

        Set<Class<?>> set = new HashSet<Class<?>>();
        for (Package pkg : packages) {

            //look for any classes annotated with @At, @EmbedAs and @With
            set.addAll(Classes.matching(
                    annotatedWith(At.class).or(
                            annotatedWith(EmbedAs.class)).or(
                            annotatedWith(With.class)).or(
                            annotatedWith(Show.class))
            ).in(pkg));
        }

        for (Class<?> clazz: set) {
            generator.generate(new DefaultServiceDefinition().extendWith(clazz));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceDefinition> defineServices(Injector injector) {
        Set<Class<?>> set = new HashSet<Class<?>>();
        for (Package pkg : packages) {
            //look for any classes annotated with @Service
            set.addAll(Classes.matching(
                    annotatedWith(Service.class)
            ).in(pkg));
        }

        List<ServiceDefinition> list = new ArrayList<ServiceDefinition>();
        // Now let's find the method that returns a ServiceDefinition
        for(Class<?> clazz : set) {

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
    public SitebricksConfig scan(Package packageName) {
        packages.add(packageName);
        return this;
    }
}
