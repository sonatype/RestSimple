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
package org.sonatype.restsimple.example.petstore;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.sonatype.restsimple.api.Action;
import org.sonatype.restsimple.api.GetServiceHandler;
import org.sonatype.restsimple.api.MediaType;
import org.sonatype.restsimple.api.PostServiceHandler;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.jaxrs.guice.JaxrsModule;

public class PetstoreServletModule extends ServletModule {

    private final static MediaType JSON = new MediaType(PetstoreAction.APPLICATION, PetstoreAction.JSON);

    @Override
    protected void configureServlets() {
        Injector injector = Guice.createInjector(new JaxrsModule(binder().withSource("[generated]")));
        Action action = new PetstoreAction();

        ServiceDefinition serviceDefinition = injector.getInstance(ServiceDefinition.class);
        serviceDefinition
                .withHandler(new GetServiceHandler("echo", action).consumeWith(JSON, Pet.class).producing(JSON))
                .withHandler(new PostServiceHandler("echo", action).consumeWith(JSON, Pet.class).producing(JSON))
                .bind();

        serve("/*").with(GuiceContainer.class);
        
    }
}

