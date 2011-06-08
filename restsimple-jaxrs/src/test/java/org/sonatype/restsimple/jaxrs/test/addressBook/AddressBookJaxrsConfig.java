/*
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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
package org.sonatype.restsimple.jaxrs.test.addressBook;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import org.sonatype.restsimple.api.Action;
import org.sonatype.restsimple.api.MediaType;
import org.sonatype.restsimple.api.PostServiceHandler;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.common.test.addressbook.AddressBookAction;
import org.sonatype.restsimple.jaxrs.guice.JaxrsConfig;

import java.util.ArrayList;
import java.util.List;

public class AddressBookJaxrsConfig extends GuiceServletContextListener {

    @Override
    protected Injector getInjector() {
        return Guice.createInjector(new JaxrsConfig() {

            @Override
            public List<ServiceDefinition> defineServices(Injector injector) {
                Action action = new AddressBookAction();
                ServiceDefinition serviceDefinition = injector.getInstance(ServiceDefinition.class);
                List<ServiceDefinition> list = new ArrayList<ServiceDefinition>();

                PostServiceHandler postServiceHandler = new PostServiceHandler("/updateAddressBook/{ad}", action);
                postServiceHandler.addFormParam("updateAddressBook");

                serviceDefinition
                        .producing(new MediaType(AddressBookAction.APPLICATION, AddressBookAction.JSON))
                        .consuming(MediaType.JSON)
                        .handleWithPut("/createAddressBook/{ad}", action)
                        .handleWithGet("/getAddressBook/{ad}", action)
                        .withHandler(postServiceHandler)
                        .handleWithDelete("/deleteAddressBook/{ad}", action);
                list.add(serviceDefinition);

                postServiceHandler = new PostServiceHandler("/updateAddressBook/{ad}", action);
                postServiceHandler.addFormParam("update");
                postServiceHandler.addFormParam("update2");

                serviceDefinition = injector.getInstance(ServiceDefinition.class);
                serviceDefinition
                        .withPath("/foo")
                        .producing(new MediaType(AddressBookAction.APPLICATION, AddressBookAction.JSON))
                        .consuming(MediaType.JSON)
                        .handleWithPut("/createAddressBook/{ad}", action)
                        .handleWithGet("/getAddressBook/{ad}", action)
                        .withHandler(postServiceHandler)
                        .handleWithDelete("/deleteAddressBook/{ad}", action);
                list.add(serviceDefinition);


                postServiceHandler = new PostServiceHandler("/updateAddressBook/{ad}", action);
                postServiceHandler
                        .producing(new MediaType(AddressBookAction.APPLICATION, AddressBookAction.JSON))
                        .consumeWith(new MediaType("text", "plain"), String.class);

                serviceDefinition = injector.getInstance(ServiceDefinition.class);
                serviceDefinition
                        .withPath("/bar")
                        .producing(new MediaType(AddressBookAction.APPLICATION, AddressBookAction.JSON))
                        .consuming(MediaType.JSON)
                        .handleWithPut("/createAddressBook/{ad}", action)
                        .handleWithGet("/getAddressBook/{ad}", action)
                        .withHandler(postServiceHandler)
                        .handleWithDelete("/deleteAddressBook/{ad}", action);
                list.add(serviceDefinition);

                return list;
            }
        });
    }
}
