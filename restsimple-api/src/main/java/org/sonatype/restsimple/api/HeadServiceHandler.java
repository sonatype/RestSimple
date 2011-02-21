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

public class HeadServiceHandler extends ServiceHandler {
    /**
     * Create a new {@link ServiceHandler}
     *
     * @param action an {@link Action} implementation
     */
    public HeadServiceHandler(Action action) {
        this(null, action);
    }

    /**
     * Create a new {@link ServiceHandler}
     *
     * @param path           a uri used to map the resource to this {@link ServiceHandler}
     * @param action an {@link Action} implementation
     */
    public HeadServiceHandler(String path, Action action) {
        this(path, action, null);
    }

    /**
     * Create a new {@link ServiceHandler}
     *
     * @param path           a uri used to map the resource to this {@link ServiceHandler}
     * @param action an {@link Action} implementation
     * @param mediaType      a {@link ServiceHandlerMediaType} that will be used when serializing the response
     */
    public HeadServiceHandler(String path, Action action, Class<? extends ServiceHandlerMediaType> mediaType) {
        super(path, action, mediaType);
    }

    @Override
    public ServiceDefinition.METHOD getHttpMethod() {
        return ServiceDefinition.METHOD.HEAD;
    }
}
