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
import org.sonatype.restsimple.common.test.petstore.Pet;
import org.sonatype.restsimple.jaxrs.guice.RestSimpleJaxrsModule;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

public class NativeJaxrsConfig extends GuiceServletContextListener {

    @Override
    protected Injector getInjector() {
        RestSimpleJaxrsModule config = new RestSimpleJaxrsModule();
        config.scan( Extension.class.getPackage() );
        return Guice.createInjector(config);
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
