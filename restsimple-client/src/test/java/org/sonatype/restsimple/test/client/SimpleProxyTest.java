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

import org.sonatype.restsimple.annotation.Consumes;
import org.sonatype.restsimple.annotation.Delete;
import org.sonatype.restsimple.annotation.Get;
import org.sonatype.restsimple.annotation.Path;
import org.sonatype.restsimple.annotation.PathParam;
import org.sonatype.restsimple.annotation.Post;
import org.sonatype.restsimple.annotation.Produces;
import org.sonatype.restsimple.client.WebException;
import org.sonatype.restsimple.client.WebProxy;
import org.sonatype.restsimple.common.test.petstore.Pet;
import org.sonatype.restsimple.common.test.petstore.PetstoreAction;
import org.testng.annotations.Test;

import java.net.URI;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.FileAssert.fail;

public abstract class SimpleProxyTest extends BaseTest {

    public static interface ProxyClient {

        @Get
        @Path("getPet")
        @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        @Consumes(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)                
        public Pet get(@PathParam("myPet") String path);

        @Get
        @Path("getPetString")
        @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        @Consumes("text/plain")
        public String getString(@PathParam("myPet") String path);

        @Post
        @Path("addPet")
        @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        @Consumes(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)        
        public Pet post(@PathParam("myPet") String myPet, String body);

        @Delete
        @Path("deletePet")
        @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        @Consumes(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)                
        public Pet delete(Pet pet, @PathParam("myPet") String petName);

    }

    public static interface ProxyClient2 {

        @Post
        @Path("addPet")
        @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        @Consumes(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        public Pet post(String body, @PathParam("myPet") String myPety);

    }

    public static interface ProxyClient3 {

        @Post
        @Path("addPet")
        @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        @Consumes(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        public Pet post(Pet body, @PathParam("myPet") String myPety);

    }

    @Test(timeOut = 20000)
    public void testBasicPostGenerate() throws Throwable {
        logger.info("running test: testPut");
        ProxyClient client = WebProxy.createProxy(ProxyClient.class, URI.create(targetUrl));
        Pet pet = client.post("myPet", "{\"name\":\"pouetpouet\"}");
        assertNotNull(pet);
    }

    @Test(timeOut = 20000)
    public void testBasicGetGenerate() throws Throwable {
        logger.info("running test: testPut");
        ProxyClient client = WebProxy.createProxy(ProxyClient.class, URI.create(targetUrl));
        Pet pet = client.post("myPet", "{\"name\":\"pouetpouet\"}");
        assertNotNull(pet);

        pet = client.get("myPet");
        assertNotNull(pet);

        String petString = client.getString("myPet");
        assertEquals(petString, "Pet{name='pouetpouet'}");
    }

    @Test(timeOut = 20000)
    public void testDelete() throws Throwable {
        logger.info("running test: testPut");
        ProxyClient client = WebProxy.createProxy(ProxyClient.class, URI.create(targetUrl));
        Pet pet = client.post("myPet", "{\"name\":\"pouetpouet\"}");
        assertNotNull(pet);

        pet = client.delete(new Pet("pouetpouet"), "myPet");
        assertNotNull(pet);

        try {
            client.getString("myPet");
            fail("No exception");
        } catch(WebException ex) {
            assertEquals(ex.getClass(), WebException.class);
        }
    }

    @Test(timeOut = 20000)
    public void testBodyOrderForPost() throws Throwable {
        logger.info("running test: testBodyOrderForPost");
        ProxyClient2 client = WebProxy.createProxy(ProxyClient2.class, URI.create(targetUrl));
        Pet pet = client.post("{\"name\":\"pouetpouet\"}", "myPet");
        assertNotNull(pet);
        assertEquals(pet.getName(), "pouetpouet");
    }

    @Test(timeOut = 20000)
    public void testRealPetPost() throws Throwable {
        logger.info("running test: testBodyOrderForPost");
        ProxyClient3 client = WebProxy.createProxy(ProxyClient3.class, URI.create(targetUrl));
        Pet pet = client.post(new Pet("pouetpouet"), "myPet");
        assertNotNull(pet);
        assertEquals(pet.getName(), "pouetpouet");
    }
}
