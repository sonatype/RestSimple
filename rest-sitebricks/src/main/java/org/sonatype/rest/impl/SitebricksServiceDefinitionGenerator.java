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
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.rest.api.ServiceDefinition;
import org.sonatype.rest.api.ResourceModuleConfig;
import org.sonatype.rest.spi.ServiceDefinitionGenerator;

/**
 * Generate a Sitebricks resource, and bind it.
 */
public class SitebricksServiceDefinitionGenerator implements ServiceDefinitionGenerator, Opcodes {

    private final ResourceModuleConfig moduleConfig;

    private final Logger logger = LoggerFactory.getLogger(SitebricksServiceDefinitionGenerator.class);

    @Inject
    public SitebricksServiceDefinitionGenerator(ResourceModuleConfig moduleConfig) {
        this.moduleConfig = moduleConfig;
    }

    @Override
    public void generate(ServiceDefinition serviceDefinition) {
        moduleConfig.bind(SitebricksResource.class);
        moduleConfig.install(new com.google.sitebricks.SitebricksModule() {
            @Override
            protected void configureSitebricks() {
                at("/:method/:id" /*serviceDefinition.path()*/).serve(SitebricksResource.class);
            }
        });
    }

}




