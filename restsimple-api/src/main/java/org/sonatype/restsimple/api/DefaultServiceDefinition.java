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
package org.sonatype.restsimple.api;

import org.sonatype.restsimple.spi.ServiceDefinitionGenerator;
import org.sonatype.restsimple.spi.ServiceHandlerMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A default {@link ServiceDefinition} which can be injected or created directly. If you aren't using injection,
 * you must specify a {@link ServiceDefinitionGenerator} if you want your {@link ServiceDefinition} to be generated and deployed
 *
 */
public class DefaultServiceDefinition implements ServiceDefinition {
    private String basePath = "";
    private final List<MediaType> mediaTypeToProduce = new ArrayList<MediaType>();
    private final List<MediaType> mediaTypeToConsume = new ArrayList<MediaType>();
    private final List<ServiceHandler> serviceHandlers = new ArrayList<ServiceHandler>();
    private ServiceDefinitionGenerator generator;
    private final ServiceHandlerMapper serviceHandlerMapper;

    public DefaultServiceDefinition() {
        this.generator = null;
        this.serviceHandlerMapper = new ServiceHandlerMapper();
    }

    public DefaultServiceDefinition(ServiceDefinitionGenerator generator) {
        this.generator = generator;
        this.serviceHandlerMapper = new ServiceHandlerMapper();
    }

    public DefaultServiceDefinition(ServiceHandlerMapper serviceHandlerMapper) {
        this.generator = null;
        this.serviceHandlerMapper = serviceHandlerMapper;
    }

    public DefaultServiceDefinition(ServiceDefinitionGenerator generator, ServiceHandlerMapper serviceHandlerMapper) {
        this.generator = generator;
        this.serviceHandlerMapper = serviceHandlerMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override    
    public ServiceDefinition withPath(String path) {
        this.basePath = path;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceDefinition withHandler(ServiceHandler serviceHandler) {
        serviceHandlers.add(serviceHandler);
        serviceHandlerMapper.addServiceHandler(serviceHandler);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceDefinition producing(MediaType mediaType) {
        mediaTypeToProduce.add(mediaType);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceDefinition consuming(MediaType mediaType) {
        mediaTypeToConsume.add(mediaType);
        return this;
    }
     /**
     * {@inheritDoc}
     */
    @Override
    public String path() {
        return basePath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceHandler> serviceHandlers() {
        return Collections.unmodifiableList(serviceHandlers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MediaType> mediaToConsume() {
        return Collections.unmodifiableList(mediaTypeToConsume);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MediaType> mediaToProduce() {
        return Collections.unmodifiableList(mediaTypeToProduce);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bind() {
        if (basePath == null) {
            throw new NullPointerException("withPath must be invoked with a non null value");
        }

        if (generator == null) {
            throw new NullPointerException("bind() must be invoked with a non null ServiceDefinitionGenerator value");
        }

        generator.generate(this);
    }

    @Override
    public ServiceDefinition generateWith(ServiceDefinitionGenerator generator) {
        this.generator = generator;
        return this;
    }

}
