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
package org.sonatype.restsimple.jaxrs.test.serviceDiscovery;

import org.sonatype.restsimple.annotation.Service;
import org.sonatype.restsimple.api.Action;
import org.sonatype.restsimple.api.GetServiceHandler;
import org.sonatype.restsimple.api.MediaType;
import org.sonatype.restsimple.api.PostServiceHandler;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.common.test.petstore.Pet;
import org.sonatype.restsimple.common.test.petstore.PetstoreAction;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Service
public class PetServiceDiscovery {
    private final static MediaType JSON = new MediaType(PetstoreAction.APPLICATION, PetstoreAction.JSON);

    @Inject
    ServiceDefinition serviceDefinition;

    public ServiceDefinition pet() {
        Action action = new PetstoreAction();

        serviceDefinition
                .withHandler(new GetServiceHandler("/get/:pet", action).consumeWith(JSON, Pet.class).producing(JSON))
                .withHandler(new PostServiceHandler("/create/:pet", action).consumeWith(JSON, Pet.class).producing(JSON))
                .extendWith(Extension.class);
        return serviceDefinition;
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
