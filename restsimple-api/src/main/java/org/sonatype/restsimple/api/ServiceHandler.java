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
package org.sonatype.restsimple.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represent how a REST resource handles requests. A {@link ServiceHandler} is used when mapping the request to
 * an {@link Action}.
 */
abstract public class ServiceHandler {
    private final String path;
    private final Action action;
    private final List<MediaType> mediaTypeToProduce = new ArrayList<MediaType>();
    private Class<?> consumerClazz;
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
     * Add a {@link org.sonatype.restsimple.api.MediaType} this ServiceDefinition. {@link org.sonatype.restsimple.api.MediaType}
     * are used when writing the response and maps the HTTP response's content-type header.
     * @param mediaType {@link org.sonatype.restsimple.api.MediaType}
     * @return the current {@link ServiceDefinition}
     */
    public ServiceHandler producing(MediaType mediaType) {
        mediaTypeToProduce.add(mediaType);
        return this;
    }

    @Override
    public String toString() {
        return "ServiceHandler{" +
                "path='" + path + '\'' +
                ", method=" + getHttpMethod() +
                ", action=" + action +
                ", mediaTypeToProduce=" + mediaTypeToProduce +
                ", consumerClazz=" + consumerClazz +
                ", consumerMediaType=" + consumerMediaType +
                '}';
    }

    /**
     *  Unmarshall the request's body into an Object of type T.
     *  @param mediaType A media type that will be used for deserializing the resquest's body.
     *  @param clazz a Class that will be instanciated and populated by the request's body.
     */
    public <T> ServiceHandler consumeWith(MediaType mediaType, Class<T> clazz) {
        consumerMediaType = mediaType;
        consumerClazz = clazz;
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
    public Class<?> consumeClass(){
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