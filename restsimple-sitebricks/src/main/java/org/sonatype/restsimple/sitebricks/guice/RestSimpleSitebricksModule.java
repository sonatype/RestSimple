/*
 * Copyright (c) 2011 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
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
    private final boolean isChild;

    public RestSimpleSitebricksModule(Binder binder,
                                      ServiceHandlerMapper mapper,
                                      NegotiationTokenGenerator tokenGenerator,
                                      Class<? extends ServiceDefinitionProvider> provider,
                                      boolean isChild) {
        this.binder = binder;
        this.mapper = mapper;
        this.tokenGenerator = tokenGenerator;
        this.provider = provider;
        this.isChild = isChild;
    }

    public RestSimpleSitebricksModule(Binder binder, NegotiationTokenGenerator tokenGenerator) {
        this(binder, new ServiceHandlerMapper(), tokenGenerator, SitebricksServiceDefinitionProvider.class, false);
    }

    public RestSimpleSitebricksModule(Binder binder, NegotiationTokenGenerator tokenGenerator, boolean isChild) {
        this(binder, new ServiceHandlerMapper(), tokenGenerator, SitebricksServiceDefinitionProvider.class, isChild);
    }

    public RestSimpleSitebricksModule(Binder binder, ServiceHandlerMapper mapper) {
        this(binder, mapper, new RFC2295NegotiationTokenGenerator(), SitebricksServiceDefinitionProvider.class, false);
    }

    public RestSimpleSitebricksModule(Binder binder, ServiceHandlerMapper mapper, boolean isChild) {
        this(binder, mapper, new RFC2295NegotiationTokenGenerator(), SitebricksServiceDefinitionProvider.class, isChild);
    }

    public RestSimpleSitebricksModule(Binder binder) {
        this(binder, new ServiceHandlerMapper(), new RFC2295NegotiationTokenGenerator(), SitebricksServiceDefinitionProvider.class, false);
    }

    public RestSimpleSitebricksModule(Binder binder, boolean isChild) {
        this(binder, new ServiceHandlerMapper(), new RFC2295NegotiationTokenGenerator(), SitebricksServiceDefinitionProvider.class, isChild);
    }

    @Override
    protected void configure() {
        // Only bind if we aren't a child of another Injector.
        if (!isChild) {
            bind(ServiceHandlerMapper.class).toInstance(mapper);
            bind(NegotiationTokenGenerator.class).toInstance(tokenGenerator);
                        bind(ServiceDefinitionGenerator.class).to(SitebricksServiceDefinitionGenerator.class);
            bind(ServiceDefinition.class).toProvider(ServiceDefinitionProvider.class);
            bind(ServiceDefinitionProvider.class).to(provider);
            binder.bind(ServiceHandlerMapper.class).toInstance(mapper);            
        }
        
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
