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

import com.google.inject.Inject;
import org.sonatype.restsimple.spi.ServiceHandlerMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A default {@link ServiceDefinition} which can be injected or created directly.
 *
 */
public class DefaultServiceDefinition implements ServiceDefinition {
    private String path = "";
    private final List<MediaType> mediaTypeToProduce = new ArrayList<MediaType>();
    private final List<MediaType> mediaTypeToConsume = new ArrayList<MediaType>();
    private final List<ServiceHandler> serviceHandlers = new ArrayList<ServiceHandler>();
    private final ServiceHandlerMapper serviceHandlerMapper;
    private final AtomicBoolean configured = new AtomicBoolean(false);
    private final List<Class<?>> extensions = new ArrayList<Class<?>>();

    public DefaultServiceDefinition() {
        this(new ServiceHandlerMapper());
    }

    @Inject
    public DefaultServiceDefinition(ServiceHandlerMapper serviceHandlerMapper) {
        this.serviceHandlerMapper = serviceHandlerMapper;
    }

    @Override
    public String toString() {
        return "DefaultServiceDefinition{" +
                "path='" + path + '\'' +
                ", mediaTypeToProduce=" + mediaTypeToProduce +
                ", mediaTypeToConsume=" + mediaTypeToConsume +
                ", serviceHandlers=" + serviceHandlers +
                ", serviceHandlerMapper=" + serviceHandlerMapper +
                '}';
    }

    /**
     * {@inheritDoc}
     */
    @Override    
    public ServiceDefinition withPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceDefinition withHandler(ServiceHandler serviceHandler) {
        serviceHandlers.add(serviceHandler);
        serviceHandlerMapper.addServiceHandler(path, serviceHandler);
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
        return path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceHandler> serviceHandlers() {

        if (!configured.getAndSet(true)) {
            for (ServiceHandler serviceHandler: serviceHandlers) {

                if (serviceHandler.mediaToProduce().size() == 0) {
                    for(MediaType p: mediaTypeToProduce) {
                        serviceHandler.producing(p);
                    }
                }

                //TODO: Could have several
                if (serviceHandler.consumeMediaType() == null && mediaTypeToConsume.size() > 0) {
                    serviceHandler.consumeWith(mediaTypeToConsume.get(0), null);
                }
            }
        }
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
        if (mediaTypeToProduce.isEmpty()) {
            mediaTypeToProduce.add(new MediaType( "text", "json"));
        }
        return Collections.unmodifiableList(mediaTypeToProduce);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceDefinition extendWith(Class<?> clazz) {
        extensions.add(clazz);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Class<?>> extensions(){
        return Collections.unmodifiableList(extensions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceDefinition handleWithGet(String path, Action action) {
        withHandler(new GetServiceHandler(path,action));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceDefinition handleWithPost(String path, Action action) {
        withHandler(new PostServiceHandler(path,action));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceDefinition handleWithPut(String path, Action action) {
        withHandler(new PutServiceHandler(path,action));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceDefinition handleWithDelete(String path, Action action) {
        withHandler(new DeleteServiceHandler(path,action));
        return this;
    }
}
