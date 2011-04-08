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
package org.sonatype.restsimple.jaxrs.test.petstore;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import org.sonatype.restsimple.api.Action;
import org.sonatype.restsimple.api.GetServiceHandler;
import org.sonatype.restsimple.api.MediaType;
import org.sonatype.restsimple.api.PostServiceHandler;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.common.test.petstore.Pet;
import org.sonatype.restsimple.common.test.petstore.PetstoreAction;
import org.sonatype.restsimple.jaxrs.guice.JaxrsConfig;

import java.util.ArrayList;
import java.util.List;

public class PetstoreJaxrsConfig extends GuiceServletContextListener {

    @Override
    protected Injector getInjector() {
        return Guice.createInjector(new JaxrsConfig() {

            private final MediaType JSON = new MediaType(PetstoreAction.APPLICATION, PetstoreAction.JSON);

            @Override
            public List<ServiceDefinition> defineServices(Injector injector) {
                Action action = new PetstoreAction();
                List<ServiceDefinition> list = new ArrayList<ServiceDefinition>();

                ServiceDefinition serviceDefinition = injector.getInstance(ServiceDefinition.class);
                serviceDefinition
                        .withHandler(new GetServiceHandler("/:echo", action).consumeWith(JSON, Pet.class).producing(JSON))
                        .withHandler(new PostServiceHandler("/:echo", action).consumeWith(JSON, Pet.class).producing(JSON));
                list.add(serviceDefinition);
                return list;
            }
        });
    }
}
