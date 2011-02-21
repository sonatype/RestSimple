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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represent how a REST resource handles requests. A {@link ServiceHandler} is used when mapping the request to
 * a {@link Action}'s method
 *
 */
abstract public class ServiceHandler {
    private final String path;
    private final Action action;
    private final Class<? extends ServiceHandlerMediaType> mediaType;
    private final List<MediaType> mediaTypeToProduce = new ArrayList<MediaType>();
    private final List<MediaType> mediaTypeToConsume = new ArrayList<MediaType>();
    
    /**
     * Create a new {@link ServiceHandler}
     *
     * @param action an {@link Action} implementation
     */
    public ServiceHandler(Action action) {
        this(null, action);
    }

    /**
     * Create a new {@link ServiceHandler}
     *
     * @param path a uri used to map the resource to this {@link ServiceHandler}
     * @param action an {@link Action} implementation
     */
    public ServiceHandler(String path, Action action) {
        this(path, action, null);
    }

    /**
     * Create a new {@link ServiceHandler}
     *
     * @param path a uri used to map the resource to this {@link ServiceHandler}
     * @param action an {@link Action} implementation
     * @param mediaType a {@link ServiceHandlerMediaType} that will be used when serializing the response
     */
    public ServiceHandler(String path, Action action, Class<? extends ServiceHandlerMediaType> mediaType) {
        this.path = path;
        this.action = action;
        this.mediaType = mediaType;
    }

    /**
     * Return the current {@link ServiceDefinition.METHOD}
     * @return {@link ServiceDefinition.METHOD}
     */
    abstract public ServiceDefinition.METHOD getHttpMethod();

    /**
     * Return the URI used to map this {@link ServiceHandler}
     * @return the URI used to map this {@link ServiceHandler}
     */
    public String getPath() {
        return path;
    }

    /**
     * Return the {@link Action} method this service will invoke.
     * @return the {@link Action} method this service will invoke.
     */
    public Action getAction() {
        return action;
    }

    /**
     * Return the {@link ServiceHandlerMediaType} used to write the response's back.
     * @return the {@link ServiceHandlerMediaType} used to write the response's back.
     */
    public Class<? extends ServiceHandlerMediaType> mediaType(){
        return mediaType;
    }
    
    /**
     * Add a {@link org.sonatype.restsimple.api.MediaType} this ServiceDefinition. {@link org.sonatype.restsimple.api.MediaType} are used when writing the response and maps the HTTP response's content-type header.
     * @param mediaType {@link org.sonatype.restsimple.api.MediaType}
     * @return the current {@link ServiceDefinition}
     */
    public ServiceHandler producing(MediaType mediaType) {
        mediaTypeToProduce.add(mediaType);
        return this;
    }

    /**
     * Add a {@link org.sonatype.restsimple.api.MediaType} this ServiceDefinition. {@link org.sonatype.restsimple.api.MediaType} are used when reading the request and maps the HTTP request's content-type header.
     * @param mediaType {@link org.sonatype.restsimple.api.MediaType}
     * @return the current {@link ServiceDefinition}
     */
    public ServiceHandler consuming(MediaType mediaType) {
        mediaTypeToConsume.add(mediaType);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public List<MediaType> mediaToConsume() {
        return Collections.unmodifiableList(mediaTypeToConsume);
    }

    /**
     * {@inheritDoc}
     */
    public List<MediaType> mediaToProduce() {
        return Collections.unmodifiableList(mediaTypeToProduce);
    }
}