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
package org.sonatype.restsimple.test.client;

import org.sonatype.restsimple.annotation.CookieParam;
import org.sonatype.restsimple.annotation.Get;
import org.sonatype.restsimple.annotation.HeaderParam;
import org.sonatype.restsimple.annotation.Path;
import org.sonatype.restsimple.annotation.PathParam;
import org.sonatype.restsimple.annotation.Post;
import org.sonatype.restsimple.annotation.Produces;
import org.sonatype.restsimple.client.WebProxy;
import org.sonatype.restsimple.common.test.petstore.Pet;
import org.sonatype.restsimple.common.test.petstore.PetstoreAction;
import org.testng.annotations.Test;

import javax.ws.rs.core.Cookie;
import java.net.URI;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public abstract class CookieParamTest extends BaseTest {

    @Test(timeOut = 20000)
    public void testBasicPostGenerate() throws Throwable {
    logger.info("running test: testPut");
        ProxyClient client = WebProxy.createProxy(ProxyClient.class, URI.create(targetUrl));
        Pet pet = client.post("myPet", "chatchien", "{\"name\":\"pouetpouet\"}");
        assertNotNull(pet);
        assertEquals(pet.getName(), "pouetpouet--chatchien");
    }

    @Test(timeOut = 20000)
    public void testBasicGetGenerate() throws Throwable {
        logger.info("running test: testPut");
        ProxyClient client = WebProxy.createProxy(ProxyClient.class, URI.create(targetUrl));
        Pet pet = client.post("myPet",  "chatchien", "{\"name\":\"pouetpouet\"}");
        assertNotNull(pet);

        pet = client.get(new Cookie("a","b","/","localhost"), "myPet");
        assertNotNull(pet);

        String petString = client.getString("myPet");
        assertEquals(petString, "{\"name\":\"pouetpouet--chatchien--$Version=1;a=b;$Domain=localhost;$Path=/\"}");
    }

    public static interface ProxyClient {

        @Get
        @Path("getPet")
        @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        public Pet get(@CookieParam("foo") Cookie cookie, @PathParam("myPet") String path);

        @Get
        @Path("getPetString")
        @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        public String getString(@PathParam("myPet") String path);

        @Post
        @Path("addPet")
        @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        public Pet post(@PathParam("myPet") String myPet, @HeaderParam(PetstoreAction.PET_EXTRA_NAME) String petType, String body);


    }


}
