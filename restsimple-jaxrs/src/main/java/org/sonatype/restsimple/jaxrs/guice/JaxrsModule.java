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
package org.sonatype.restsimple.jaxrs.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Module;
import org.sonatype.restsimple.spi.ResourceModuleConfig;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.jaxrs.impl.JAXRSServiceDefinitionGenerator;
import org.sonatype.restsimple.jaxrs.impl.JAXRSServiceDefinitionProvider;
import org.sonatype.restsimple.spi.ServiceDefinitionGenerator;
import org.sonatype.restsimple.spi.ServiceDefinitionProvider;
import org.sonatype.restsimple.spi.ServiceHandlerMapper;

/**
 * A JAXRS module that install the appropriate object needed to generate JAXRS Resource.
 */
public class JaxrsModule extends AbstractModule {

    private final Binder binder;

    public JaxrsModule(Binder binder) {
        this.binder = binder;
    }

    @Override
    protected void configure() {
        /**
         * We MUST bin the mapper with both Binder to be able to share the same instance.
         */
        final ServiceHandlerMapper mapper = new ServiceHandlerMapper();
        bind(ServiceHandlerMapper.class).toInstance(mapper);
        binder.bind(ServiceHandlerMapper.class).toInstance(mapper);

        bind(ResourceModuleConfig.class).toInstance(new ResourceModuleConfig<Module>() {

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
                binder.bind(clazz).asEagerSingleton();
            }

            @Override
            public void install(Module module) {
            }
        });

        bind(ServiceDefinitionGenerator.class).to(JAXRSServiceDefinitionGenerator.class);
        bind(ServiceDefinition.class).toProvider(ServiceDefinitionProvider.class);
        bind(ServiceDefinitionProvider.class).to(JAXRSServiceDefinitionProvider.class);

    }
}
