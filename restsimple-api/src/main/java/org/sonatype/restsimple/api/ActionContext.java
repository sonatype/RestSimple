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

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * This class encapsulates the REST request, e.g the headers, the form parameters, the path and it's value. It also
 * contains the request's body typed object. The deserialization occurs before an {@link Action#action(ActionContext)}
 * gets invoked.
 */
public class ActionContext<T> {

    private final Map<String, Collection<String>> matrixStrings;
    private final Map<String, Collection<String>> paramsStrings;
    private final Map<String, Collection<String>> headers;
    private final InputStream inputStream;
    private final ServiceDefinition.METHOD methodName;
    private final Map<String,String> pathParams;
    private final T object;

    public ActionContext(ServiceDefinition.METHOD methodName,
                         Map<String, Collection<String>> headers,
                         Map<String, Collection<String>> paramsStrings,
                         InputStream inputStream,
                         Map<String,String> pathParams,
                         T object) {

        this.paramsStrings = paramsStrings;
        this.methodName = methodName;
        this.inputStream = inputStream;
        this.headers = headers;
        this.object = object;
        this.matrixStrings = Collections.emptyMap();
        this.pathParams = pathParams;
    }

    public ActionContext(ServiceDefinition.METHOD methodName,
                         Map<String, Collection<String>> headers,
                         Map<String, Collection<String>> paramsStrings,
                         Map<String, Collection<String>> matrixStrings,
                         InputStream inputStream,
                         Map<String,String> pathParams,
                         T object) {

        this.paramsStrings = paramsStrings;
        this.methodName = methodName;
        this.inputStream = inputStream;
        this.headers = headers;
        this.object = object;
        this.matrixStrings = matrixStrings;
        this.pathParams = pathParams;
    }

    /**
     * Return the matrix if they were specified.
     *
     * @return a {@link Map} of parameters.
     */
    public Map<String, Collection<String>> matrixString() {
        return Collections.unmodifiableMap(matrixStrings);

    }

    /**
     * Return the parameters if they were specified.
     *
     * @return a {@link Map} of parameters.
     */
    public Map<String, Collection<String>> paramsString() {
        return Collections.unmodifiableMap(paramsStrings);

    }

    /**
     * Return the request headers.
     *
     * @return a {@link Map} of request headers
     */
    public Map<String, Collection<String>> headers() {
        return Collections.unmodifiableMap(headers);
    }

    /**
     * Return the request's input stream. If there is a body, use this method to read it.
     *
     * @return the request's input stream
     */
    public InputStream inputStream() {
        return inputStream;
    }

    /**
     * Return the current method used.
     *
     * @return the current method used.
     */
    public ServiceDefinition.METHOD method() {
        return methodName;
    }

    /**
     * Return all the path parameters and their associated value.
     * @return all the path parameters
     */
    public Map<String,String> pathParams() {
        return pathParams;
    }

    /**
     * Return the T defined by the {@link ServiceHandler#consumeMediaType()};
     *
     * @return the T defined by the {@link org.sonatype.restsimple.api.ServiceHandler#consumeMediaType()};
     */
    public T get() {
        return object;
    }
}
