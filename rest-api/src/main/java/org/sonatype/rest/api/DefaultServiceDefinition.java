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
package org.sonatype.rest.api;

import org.sonatype.rest.spi.ServiceDefinitionGenerator;
import org.sonatype.rest.spi.ServiceHandlerMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultServiceDefinition implements ServiceDefinition {
    private String basePath = "";
    private ServiceEntity serviceEntity;
    private final List<Media> mediaToProduce = new ArrayList<Media>();
    private final List<Media> mediaToConsume = new ArrayList<Media>();
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
    public ServiceDefinition usingEntity(ServiceEntity serviceEntity) {
        this.serviceEntity = serviceEntity;
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
    public ServiceDefinition producing(Media media) {
        mediaToProduce.add(media);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceDefinition consuming(Media media) {
        mediaToConsume.add(media);
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
    public ServiceEntity serviceEntity() {
        return serviceEntity;
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
    public List<Media> mediaToConsume() {
        return Collections.unmodifiableList(mediaToConsume);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Media> mediaToProduce() {
        return Collections.unmodifiableList(mediaToProduce);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void bind() {
        if (basePath == null) {
            throw new NullPointerException("withPath must be invoked with a non null value");
        }

        if (serviceEntity == null) {
            throw new NullPointerException("usingEntity must be invoked with a non null value");
        }

        if (generator == null) {
            throw new NullPointerException("generateWith must be invoked with a non null value");
        }

        generator.generate(this);

    }

    @Override
    public ServiceDefinition generateWith(ServiceDefinitionGenerator generator) {
        this.generator = generator;
        return this;
    }
}
