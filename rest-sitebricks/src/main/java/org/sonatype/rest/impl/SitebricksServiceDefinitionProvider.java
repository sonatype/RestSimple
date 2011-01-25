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
package org.sonatype.rest.impl;

import com.google.inject.Inject;
import org.sonatype.rest.api.DefaultServiceDefinition;
import org.sonatype.rest.api.ServiceDefinition;
import org.sonatype.rest.spi.ServiceDefinitionGenerator;
import org.sonatype.rest.spi.ServiceDefinitionProvider;
import org.sonatype.rest.spi.ServiceHandlerMapper;

public class SitebricksServiceDefinitionProvider implements ServiceDefinitionProvider {

    @Inject
    public ServiceDefinitionGenerator generator;

    @Inject
    ServiceHandlerMapper mapper;

    @Override
    public ServiceDefinition get() {
        return new DefaultServiceDefinition(generator, mapper);
    }
}
