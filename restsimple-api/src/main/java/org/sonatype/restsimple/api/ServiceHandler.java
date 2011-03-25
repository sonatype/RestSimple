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
 * an {@link Action}.
 */
abstract public class

        ServiceHandler {
    private final String path;
    private final Action action;
    private Class producerClass;
    private final List<MediaType> mediaTypeToProduce = new ArrayList<MediaType>();
    private Class consumerClazz;
    private MediaType consumerMediaType;
    
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
        this.path = path;
        this.action = action;
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
    public String path() {
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
     * Return the {@link Class} to be used when serializing the response's body
     * @return the {@link Class} to be used when serializing the response's body.
     */
    public Class producerClass(){
        return producerClass;
    }
    
    /**
     * Add a {@link org.sonatype.restsimple.api.MediaType} this ServiceDefinition. {@link org.sonatype.restsimple.api.MediaType}
     * are used when writing the response and maps the HTTP response's content-type header.
     * @param mediaType {@link org.sonatype.restsimple.api.MediaType}
     * @return the current {@link ServiceDefinition}
     */
    public ServiceHandler producing(MediaType mediaType) {
        mediaTypeToProduce.add(mediaType);
        return this;
    }

    /**
     *  Unmarshall the request's body into an Object of type T.
     */
    public <T> ServiceHandler consumeWith(MediaType mediaType, Class<T> object) {
        consumerMediaType = mediaType;
        consumerClazz = object;
        return this;
    }

    /**
     * Return the {@link MediaType} used to unmarshall the request's body.
     * @return the {@link MediaType} used to unmarshall the request's body.
     */
    public MediaType consumeMediaType() {
        return consumerMediaType;
    }

    /**
     * Return the class that will be used to unmarshall the request's body.
     * @return the class that will be used to unmarshall the request's body.
     */
    public Class consumeClass(){
       return consumerClazz;
    }

    /**
     * Return an unmodifiable {@link List} of {@link org.sonatype.restsimple.api.MediaType}
     * @return an unmodifiable {@link List} of {@link org.sonatype.restsimple.api.MediaType}
     */
    public List<MediaType> mediaToProduce() {
        return Collections.unmodifiableList(mediaTypeToProduce);
    }
}