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
package org.sonatype.restsimple.jaxrs.test.nativejaxrs;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import org.sonatype.restsimple.api.MediaType;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.common.test.petstore.Pet;
import org.sonatype.restsimple.common.test.petstore.PetstoreAction;
import org.sonatype.restsimple.jaxrs.guice.JaxrsConfig;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.List;

public class NativeJaxrsConfig extends GuiceServletContextListener {

    @Override
    protected Injector getInjector() {
        return Guice.createInjector(new JaxrsConfig() {

            private final MediaType JSON = new MediaType(PetstoreAction.APPLICATION, PetstoreAction.JSON);

            @Override
            public List<ServiceDefinition> defineServices(Injector injector) {
                List<ServiceDefinition> list = new ArrayList<ServiceDefinition>();

                ServiceDefinition serviceDefinition = injector.getInstance(ServiceDefinition.class);
                serviceDefinition.extendWith(Extension.class);

                list.add(serviceDefinition);
                return list;
            }
        });
    }

    @Path("/lolipet/{myPet}")
    public final static class Extension {
        @GET
        @Consumes("application/vnd.org.sonatype.rest+json")
        public Pet lolipet(){
            return new Pet("lolipet");
        }
    }
}
