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

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Module;
import org.sonatype.restsimple.spi.NegotiationTokenGenerator;
import org.sonatype.restsimple.spi.RFC2295NegotiationTokenGenerator;
import org.sonatype.restsimple.spi.ResourceModuleConfig;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.sitebricks.impl.SitebricksServiceDefinitionGenerator;
import org.sonatype.restsimple.sitebricks.impl.SitebricksServiceDefinitionProvider;
import org.sonatype.restsimple.spi.ServiceDefinitionGenerator;
import org.sonatype.restsimple.spi.ServiceDefinitionProvider;
import org.sonatype.restsimple.spi.ServiceHandlerMapper;

/**
 * A Sitebricks module that install the appropriate object needed to generate Sitebricks Resource.
 */
public class RestSimpleSitebricksModule extends AbstractModule {

    private final Binder binder;
    private final ServiceHandlerMapper mapper;
    private final NegotiationTokenGenerator tokenGenerator;
    private final Class<? extends ServiceDefinitionProvider> provider;

    public RestSimpleSitebricksModule(Binder binder,
                                      ServiceHandlerMapper mapper,
                                      NegotiationTokenGenerator tokenGenerator,
                                      Class<? extends ServiceDefinitionProvider> provider) {
        this.binder = binder;
        this.mapper = mapper;
        this.tokenGenerator = tokenGenerator;
        this.provider = provider;
    }

    public RestSimpleSitebricksModule(Binder binder, NegotiationTokenGenerator tokenGenerator) {
        this(binder, new ServiceHandlerMapper(), tokenGenerator, SitebricksServiceDefinitionProvider.class);
    }

    public RestSimpleSitebricksModule(Binder binder, ServiceHandlerMapper mapper) {
        this(binder, mapper, new RFC2295NegotiationTokenGenerator(), SitebricksServiceDefinitionProvider.class);
    }

    public RestSimpleSitebricksModule(Binder binder) {
        this(binder, new ServiceHandlerMapper(), new RFC2295NegotiationTokenGenerator(), SitebricksServiceDefinitionProvider.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {

        bind(ServiceHandlerMapper.class).toInstance(mapper);
        bind(NegotiationTokenGenerator.class).toInstance(tokenGenerator);
        bind(ServiceDefinitionGenerator.class).to(SitebricksServiceDefinitionGenerator.class);
        bind(ServiceDefinition.class).toProvider(ServiceDefinitionProvider.class);
        bind(ServiceDefinitionProvider.class).to(provider);
        binder.bind(ServiceHandlerMapper.class).toInstance(mapper);
        binder.bind(NegotiationTokenGenerator.class).toInstance(tokenGenerator);

        bind(ResourceModuleConfig.class).toInstance(new ResourceModuleConfig<Module>(){

            @Override
            public <A> void bindToInstance(Class<A> clazz, A instance) {
                binder.bind(clazz).toInstance(instance);
            }

            @Override
            public <A> void bindTo(Class<A> clazz, Class<? extends A> clazz2) {
                binder.bind(clazz).to(clazz2);
            }

            @Override
            public void bind(Class<?> clazz) {
                binder.bind(clazz);
            }

            @Override
            public void install(Module module) {
                binder.install(module);
            }
        });
        
    }
}
