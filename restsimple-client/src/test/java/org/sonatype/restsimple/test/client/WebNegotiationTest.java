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

import org.sonatype.restsimple.api.MediaType;
import org.sonatype.restsimple.client.WebAHCClient;
import org.sonatype.restsimple.client.WebClient;
import org.sonatype.restsimple.common.test.petstore.Pet;
import org.sonatype.restsimple.common.test.petstore.PetstoreAction;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertNotNull;

public abstract class WebNegotiationTest extends BaseTest {

    @Test(timeOut = 20000)
    public void testPostContentNegotiation() throws Throwable {
        logger.info("running test: testPostWithType");

        WebClient webClient = new WebAHCClient(serviceDefinition);
        Map<String, String> m = new HashMap<String, String>();
        m.put("Content-Type", acceptHeader);
        m.put("Accept", "application/xml");

        Pet pet = webClient.clientOf(targetUrl + "/addPet/myPet")
                .headers(m)
                .supportedContentType(new MediaType(PetstoreAction.APPLICATION, PetstoreAction.JSON))
                .post(new Pet("pouetpouet"), Pet.class);

        assertNotNull(pet);

        pet = webClient.clientOf(targetUrl + "/getPet/myPet")
                .headers(m)
                .get(Pet.class);

        assertNotNull(pet);
    }
}
