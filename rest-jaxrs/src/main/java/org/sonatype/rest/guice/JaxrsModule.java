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
package org.sonatype.rest.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import org.sonatype.rest.spi.ResourceBinder;
import org.sonatype.rest.api.ServiceDefinition;
import org.sonatype.rest.spi.ServiceDefinitionGenerator;
import org.sonatype.rest.spi.ServiceDefinitionProvider;
import org.sonatype.rest.spi.ServiceHandlerMapper;
import org.sonatype.rest.impl.JAXRSServiceDefinitionGenerator;
import org.sonatype.rest.impl.JAXRSServiceDefinitionProvider;

public class JaxrsModule extends AbstractModule {

    private final Binder binder;

    public JaxrsModule(Binder binder) {
        this.binder = binder;
    }

    @Override
    protected void configure() {
        final ServiceHandlerMapper mapper = new ServiceHandlerMapper();
        bind(ServiceHandlerMapper.class).toInstance(mapper);
        
        bind(ResourceBinder.class).toInstance(new ResourceBinder(){

            @Override
            public void bind(Class<?> clazz) {
                binder.bind(ServiceHandlerMapper.class).toInstance(mapper);
                binder.bind(clazz);
            }
        });

        
        bind(ServiceDefinitionGenerator.class).to(JAXRSServiceDefinitionGenerator.class);
        bind(ServiceDefinition.class).toProvider(ServiceDefinitionProvider.class);
        bind(ServiceDefinitionProvider.class).to(JAXRSServiceDefinitionProvider.class);
        
    }
}
