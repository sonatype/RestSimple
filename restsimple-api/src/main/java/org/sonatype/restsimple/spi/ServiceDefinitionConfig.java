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
package org.sonatype.restsimple.spi;

import com.google.inject.Injector;
import org.sonatype.restsimple.api.ServiceDefinition;

import java.util.List;

/**
 * As simple interface allowing applications to define a list of {@link ServiceDefinition} to deploy, and configure
 * the server side functionality.
 * 
 */
public interface ServiceDefinitionConfig {

    /**
     * Return the a list of ServiceDefinition.
     * @param injector a Guice {@link Injector}
     * @return the a list of ServiceDefinition.
     */
    List<ServiceDefinition> defineServices(Injector injector);

    /**
     * Configure the {@link NegotiationTokenGenerator} used for content/version negotiation.
     * @return an instance of {@link NegotiationTokenGenerator}
     */
    NegotiationTokenGenerator configureNegotiationTokenGenerator();

    /**
     * Return the {@link Injector} used to create the module
     * @return the {@link Injector} used to create the module
     */
    Injector injector();

}
