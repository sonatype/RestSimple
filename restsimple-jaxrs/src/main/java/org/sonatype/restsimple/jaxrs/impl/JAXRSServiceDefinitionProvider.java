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
package org.sonatype.restsimple.jaxrs.impl;

import com.google.inject.Inject;
import org.sonatype.restsimple.api.DefaultServiceDefinition;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.spi.ServiceDefinitionProvider;
import org.sonatype.restsimple.spi.ServiceHandlerMapper;

public class JAXRSServiceDefinitionProvider implements ServiceDefinitionProvider {

    @Inject
    ServiceHandlerMapper mapper;
    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceDefinition get() {
        return new DefaultServiceDefinition(mapper);
    }
}
