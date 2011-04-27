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

import org.sonatype.restsimple.client.WebClient;
import org.sonatype.restsimple.client.WebException;
import org.sonatype.restsimple.client.WebAHCClient;
import org.sonatype.restsimple.common.test.petstore.Pet;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.FileAssert.fail;

public abstract class WebTest extends BaseTest {

//    @Test(timeOut = 20000)
//    public void testPost() throws Throwable {
//        logger.info("running test: testPost");
//
//        WebClient webClient = new WebAHCClient(serviceDefinition);
//        Map<String, String> m = new HashMap<String, String>();
//        m.put("Content-Type", acceptHeader);
//
//        Pet pet = (Pet) webClient.clientOf(targetUrl + "/addPet/myPet")
//                .headers(m)
//                .post(new Pet("pouetpouet"));
//
//        assertNotNull(pet);
//
//        pet = webClient.clientOf(targetUrl + "/getPet/myPet")
//                .headers(m)
//                .get(Pet.class);
//
//        assertNotNull(pet);
//    }
//
//    @Test(timeOut = 20000)
//    public void testDelete() throws Throwable {
//        logger.info("running test: testDelete");
//
//        WebClient webClient = new WebAHCClient(serviceDefinition);
//        Map<String, String> m = new HashMap<String, String>();
//        m.put("Content-Type", acceptHeader);
//
//        Pet pet = (Pet) webClient.clientOf(targetUrl + "/addPet/myPet")
//                .headers(m)
//                .post(new Pet("pouetpouet"));
//
//        assertNotNull(pet);
//
//        pet = (Pet) webClient.clientOf(targetUrl + "/deletePet/myPet")
//                .headers(m)
//                .delete(new Pet("pouetpouet"));
//
//        assertNotNull(pet);
//
//        try {
//            pet = (Pet) webClient.clientOf(targetUrl + "/getPet/myPet")
//                    .headers(m)
//                    .get();
//            fail("No exception");
//        } catch (WebException ex) {
//            assertEquals(ex.getClass(), WebException.class);
//        }
//
//    }
//
//    @Test(timeOut = 20000)
//    public void testPostWithType() throws Throwable {
//        logger.info("running test: testPostWithType");
//
//        WebClient webClient = new WebAHCClient(serviceDefinition);
//        Map<String, String> m = new HashMap<String, String>();
//        m.put("Content-Type", acceptHeader);
//
//        Pet pet = webClient.clientOf(targetUrl + "/addPet/myPet")
//                .headers(m)
//                .post(new Pet("pouetpouet"), Pet.class);
//
//        assertNotNull(pet);
//
//        pet = webClient.clientOf(targetUrl + "/getPet/myPet")
//                .headers(m)
//                .get(Pet.class);
//
//        assertNotNull(pet);
//    }
//
//    @Test(timeOut = 20000)
//    public void testPostWithoutSD() throws Throwable {
//        logger.info("running test: testPostWithoutSD");
//
//        WebClient webClient = new WebAHCClient();
//        Map<String, String> m = new HashMap<String, String>();
//        m.put("Content-Type", acceptHeader);
//        m.put("Accept", acceptHeader);
//
//        Pet pet = webClient.clientOf(targetUrl + "/addPet/myPet")
//                .headers(m)
//                .post(new Pet("pouetpouet"), Pet.class);
//
//        assertNotNull(pet);
//
//        pet = webClient.clientOf(targetUrl + "/getPet/myPet")
//                .headers(m)
//                .get(Pet.class);
//
//        assertNotNull(pet);
//    }

    @Test(timeOut = 200000)
    public void testPostSerializeDeserialize() throws Throwable {
        logger.info("running test: testPost");

        WebClient webClient = new WebAHCClient(serviceDefinition);
        Map<String, String> m = new HashMap<String, String>();
        m.put("Content-Type", acceptHeader);

        Pet pet = (Pet) webClient.clientOf(targetUrl + "/addPet/myPet")
                .headers(m)
                .post("{\"name\":\"pouetpouetpouetpouet\"}",  Pet.class);

        assertNotNull(pet);

        pet = webClient.clientOf(targetUrl + "/getPet/myPet")
                .headers(m)
                .get(Pet.class);

        assertNotNull(pet);
    }

}
