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
package org.sonatype.restsimple.spi;

import com.google.inject.Injector;
import org.sonatype.restsimple.api.ServiceDefinition;

import java.util.List;

/**
 * A simple interface allowing applications to define a list of {@link ServiceDefinition} to deploy, and configure
 * the server side functionality.
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

    /**
     * Scan for native REST resource located in the package name
     *
     * @param packageName the package name
     */
    void scan(Package packageName);

}
