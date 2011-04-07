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

import org.sonatype.restsimple.api.ServiceDefinition;

/**
 * Generates a resource based on the information a {@link org.sonatype.restsimple.api.ServiceDefinition} represents.
 *
 * This class is targetted at framework integrator.
 */
public interface ServiceDefinitionGenerator {

    /**
     * Generate a REST resource based on the information a {@link org.sonatype.restsimple.api.ServiceDefinition} represents.
     * @param serviceDefinition a {@link ServiceDefinition}
     */
    void generate(ServiceDefinition serviceDefinition);

}
