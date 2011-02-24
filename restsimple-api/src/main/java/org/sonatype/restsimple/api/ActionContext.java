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

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

/**
 * This class encapsulates the REST request, e.g the headers, the form parameters, the path and it's value.
 */
public class ActionContext<T> {

    private final Map<String, Collection<String>> formParam;
    private final Map<String, Collection<String>> headers;
    private final InputStream inputStream;
    private final ServiceDefinition.METHOD methodName;
    private final String pathName;
    private final String pathValue;
    private final T object;

    public ActionContext(ServiceDefinition.METHOD methodName, Map<String, Collection<String>> headers, Map<String, Collection<String>> formParam, InputStream inputStream, String pathName, String pathValue, T object) {
        this.formParam = formParam;
        this.methodName = methodName;
        this.inputStream = inputStream;
        this.headers = headers;
        this.pathName = pathName;
        this.pathValue = pathValue;
        this.object = object;
    }

    /**
     * Return the form params if they were specified.
     * @return a {@link Map} of form parameters.
     */
    public Map<String, Collection<String>> formParams() {
        return formParam;

    }

    /**
     * Return the request headers.
     * @return a {@link Map} of request headers
     */
    public Map<String, Collection<String>> headers() {
        return headers;
    }

    /**
     * Return the request's input stream. If there is a body, use this method to read it.
     * @return the request's input stream
     */
    public InputStream inputStream() {
        return inputStream;
    }

    /**
     * Return the current method used.
     * @return the current method used.
     */
    public ServiceDefinition.METHOD method() {
        return methodName;
    }

    /**
     * Return the current path name used.
     * @return the current path name used.
     */
    public String pathValue(){
        return pathValue;
    }

    /**
     * Return the current path's value
     * @return the current path's value
     */
    public String pathName(){
        return pathName;
    }

    /**
     * Return the T defined by the {@link org.sonatype.restsimple.api.ServiceHandler#mediaType()};
     * @return the T defined by the {@link org.sonatype.restsimple.api.ServiceHandler#mediaType()};
     */
    public T get() {
        return object;
    }
}