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

/**
 * Represent how a REST resource handles requests. A {@link ServiceHandler} is used when mapping the request to
 * a {@link ServiceEntity}
 *
 * TODO: make me a builder
 */
public class ServiceHandler {
    private final ServiceDefinition.HttpMethod httpMethod;
    private final String path;
    private final String method;
    private final Class<? extends ServiceHandlerMediaType> mediaType;

    /**
     * Create a new {@link ServiceHandler}
     *
     * @param httpMethod an {@link ServiceDefinition.HttpMethod}
     * @param methodToInvoke a method's name used to invoke a {@link ServiceEntity}
     */
    public ServiceHandler(ServiceDefinition.HttpMethod httpMethod, String methodToInvoke) {
        this(httpMethod, null, methodToInvoke);
    }

    /**
     * Create a new {@link ServiceHandler}
     *
     * @param httpMethod an {@link ServiceDefinition.HttpMethod}
     * @param path a uri used to map the resource to this {@link ServiceHandler}
     * @param methodToInvoke a methodToInvoke's name used to invoke a {@link ServiceEntity}
     */
    public ServiceHandler(ServiceDefinition.HttpMethod httpMethod, String path, String methodToInvoke) {
        this(httpMethod, path, methodToInvoke, null);
    }

    /**
     * Create a new {@link ServiceHandler}
     *
     * @param httpMethod an {@link ServiceDefinition.HttpMethod}
     * @param path a uri used to map the resource to this {@link ServiceHandler}
     * @param methodToInvoke a methodToInvoke's name used to invoke a {@link ServiceEntity}
     * @param mediaType a {@link ServiceHandlerMediaType} that will be used when serializing the response
     */
    public ServiceHandler(ServiceDefinition.HttpMethod httpMethod, String path, String methodToInvoke, Class<? extends ServiceHandlerMediaType> mediaType) {
        this.httpMethod = httpMethod;
        this.path = path;
        this.method = methodToInvoke;
        this.mediaType = mediaType;
    }

    /**
     * Return the current {@link ServiceDefinition.HttpMethod}
     * @return {@link ServiceDefinition.HttpMethod}
     */
    public ServiceDefinition.HttpMethod getHttpMethod() {
        return httpMethod;
    }

    /**
     * Return the URI used to map this {@link ServiceHandler}
     * @return the URI used to map this {@link ServiceHandler}
     */
    public String getPath() {
        return path;
    }

    /**
     * Return the HTTP method.
     * @return the HTTP method.
     */
    public String getMethod() {
        return method;
    }

    /**
     * Return the {@link ServiceHandlerMediaType} used to write the response's back.
     * @return the {@link ServiceHandlerMediaType} used to write the response's back.
     */
    public Class<? extends ServiceHandlerMediaType> mediaType(){
        return mediaType;
    }

}