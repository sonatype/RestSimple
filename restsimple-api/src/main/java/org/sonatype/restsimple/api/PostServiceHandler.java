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
 * A {@link ServiceHandler} that support the POST HTTP method
 */
public class PostServiceHandler extends ServiceHandler {

    private final ArrayList<String> formParam = new ArrayList<String>();

    /**
     * Create a new {@link ServiceHandler}
     *
     * @param action an {@link Action} implementation
     */
    public PostServiceHandler(Action action) {
        this("/", action);
    }

    /**
     * Create a new {@link ServiceHandler}
     *
     * @param path   a uri used to map the resource to this {@link ServiceHandler}
     * @param action an {@link Action} implementation
     */
    public PostServiceHandler(String path, Action action) {
        super(path, action);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceDefinition.METHOD getHttpMethod() {
        return ServiceDefinition.METHOD.POST;
    }

    /**
     * Add a form parameters.
     * @param name a form parameters
     * @return this
     */
    public PostServiceHandler addFormParam(String name) {
        formParam.add(name);
        return this;
    }

    /**
     * 'Return an unmodifiable {@link List} of form parameters
     * @return
     */
    public List<String> formParams() {
        return Collections.unmodifiableList(formParam);
    }
}
