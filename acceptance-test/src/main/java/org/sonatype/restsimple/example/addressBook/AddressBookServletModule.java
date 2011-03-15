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
package org.sonatype.restsimple.example.addressBook;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.sonatype.restsimple.api.Action;
import org.sonatype.restsimple.api.DeleteServiceHandler;
import org.sonatype.restsimple.api.GetServiceHandler;
import org.sonatype.restsimple.api.MediaType;
import org.sonatype.restsimple.api.PostServiceHandler;
import org.sonatype.restsimple.api.PutServiceHandler;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.common.test.AddressBookAction;
import org.sonatype.restsimple.jaxrs.guice.JaxrsModule;

public class AddressBookServletModule extends com.google.inject.servlet.ServletModule {

    @Override
    protected void configureServlets() {
        Injector injector = Guice.createInjector(new JaxrsModule(binder().withSource("[generated]")));

        Action action = new AddressBookAction();
        ServiceDefinition serviceDefinition = injector.getInstance(ServiceDefinition.class);

        PostServiceHandler postServiceHandler = new PostServiceHandler("updateAddressBook", action);
        postServiceHandler.addFormParam("update");

        serviceDefinition
                .producing(new MediaType(AddressBookAction.APPLICATION, AddressBookAction.JSON))
                .producing(new MediaType(AddressBookAction.APPLICATION, AddressBookAction.XML))
                .consuming(MediaType.JSON)
                .consuming(MediaType.XML)
                .withHandler(new PutServiceHandler("createAddressBook", action))
                .withHandler(new GetServiceHandler("getAddressBook", action))
                .withHandler(postServiceHandler)
                .withHandler(new DeleteServiceHandler("deleteAddressBook", action))
                .bind();


        postServiceHandler = new PostServiceHandler("updateAddressBook", action);
        postServiceHandler.addFormParam("update");
        postServiceHandler.addFormParam("update2");

        serviceDefinition = injector.getInstance(ServiceDefinition.class);
        serviceDefinition
                .withPath("/foo")
                .producing(new MediaType(AddressBookAction.APPLICATION, AddressBookAction.JSON))
                .producing(new MediaType(AddressBookAction.APPLICATION, AddressBookAction.XML))
                .consuming(MediaType.JSON)
                .consuming(MediaType.XML)
                .withHandler(new PutServiceHandler("createAddressBook", action))
                .withHandler(new GetServiceHandler("getAddressBook", action))
                .withHandler(postServiceHandler)
                .withHandler(new DeleteServiceHandler("deleteAddressBook", action))
                .bind();

        postServiceHandler = new PostServiceHandler("updateAddressBook", action);

        serviceDefinition = injector.getInstance(ServiceDefinition.class);
        serviceDefinition
                .withPath("/bar")
                .producing(new MediaType(AddressBookAction.APPLICATION, AddressBookAction.JSON))
                .producing(new MediaType(AddressBookAction.APPLICATION, AddressBookAction.XML))
                .consuming(MediaType.JSON)
                .consuming(MediaType.XML)
                .withHandler(new PutServiceHandler("createAddressBook", action))
                .withHandler(new GetServiceHandler("getAddressBook", action))
                .withHandler(postServiceHandler)
                .withHandler(new DeleteServiceHandler("deleteAddressBook", action))
                .bind();
        
        serve("/*").with(GuiceContainer.class);

    }

}

