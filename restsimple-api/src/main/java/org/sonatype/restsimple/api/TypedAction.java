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

/**
 * Simple {@link Action} that delegate the HTTP method operation to proper method implementation of GET, POST, PUT and
 * DELETE. By default all method are throwing an exception with the status code set to 405 (NOT SUPPORTED). Override the
 * proper method.
 *
 * @param <T>
 * @param <V>
 */
public class TypedAction<T, V> implements Action<T, V> {

    @Override
    public T action(ActionContext<V> actionContext) throws ActionException {
        switch (actionContext.method()) {
            case GET:
                return get(actionContext);
            case PUT:
                return put(actionContext);
            case POST:
                return post(actionContext);
            case DELETE:
                return delete(actionContext);
        }
        throw new ActionException(405);
    }

    /**
     * Handle a Get operation
     * @param actionContext an {@link ActionContext}
     * @return the response body to serialize.
     */
    public T get(ActionContext<V> actionContext) {
        return get(actionContext.get());
    }

    /**
     * Handle a PUT operation
     * @param actionContext an {@link ActionContext}
     * @return the response body to serialize.
     */
    public T put(ActionContext<V> actionContext) {
        return put(actionContext.get());
    }

    /**
     * Handle a POST operation
     * @param actionContext an {@link ActionContext}
     * @return the response body to serialize.
     */
    public T post(ActionContext<V> actionContext) {
        return post(actionContext.get());
    }

    /**
     * Handle a Handle operation
     * @param actionContext an {@link ActionContext}
     * @return the response body to serialize.
     */
    public T delete(ActionContext<V> actionContext) {
        return delete(actionContext.get());
    }

    /**
     * Handle a Get operation
     * @param u the request's body deserialized
     * @return the response body to serialize.
     */
    public T get(V u) {
        throw new ActionException(405);
    }

    /**
     * Handle a PUT operation
     * @param u the request's body deserialized
     * @return the response body to serialize.
     */
    public T put(V u) {
        throw new ActionException(405);
    }

    /**
     * Handle a POST operation
     * @param u the request's body deserialized
     * @return the response body to serialize.
     */
    public T post(V u) {
        throw new ActionException(405);
    }

    /**
     * Handle a Handle operation
     * @param u the request's body deserialized
     * @return the response body to serialize.
     */
    public T delete(V u) {
        throw new ActionException(405);
    }

}


