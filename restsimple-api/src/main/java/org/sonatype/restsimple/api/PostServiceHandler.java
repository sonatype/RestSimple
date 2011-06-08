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
