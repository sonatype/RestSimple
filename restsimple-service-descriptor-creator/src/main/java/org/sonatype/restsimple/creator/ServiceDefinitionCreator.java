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
package org.sonatype.restsimple.creator;

import org.sonatype.restsimple.api.ServiceDefinition;

/**
 * Generate {@link ServiceDefinition} from a class.
 */
public interface ServiceDefinitionCreator {

    /**
     * Generate {@link ServiceDefinition} from a class. The default mapping described in {@link ServiceDefinitionCreatorConfig}
     * will be used.
     * @param application any class.
     * @return {@link ServiceDefinition}
     * @throws Exception
     * @throws IllegalAccessException
     */
    ServiceDefinition create(Class<?> application) throws Exception, IllegalAccessException;

    /**
     * Generate {@link ServiceDefinition} from a class using {@link ServiceDefinitionCreatorConfig} as an hint.
     * will be used.
     * @param application any class.
     * @param config a {@link ServiceDefinitionCreatorConfig}
     * @return {@link ServiceDefinition}
     * @throws Exception
     * @throws IllegalAccessException
     */
    ServiceDefinition create(Class<?> application, ServiceDefinitionCreatorConfig config) throws Exception, IllegalAccessException;

}
