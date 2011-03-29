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
package org.sonatype.restsimple.test.webdriver.org.sonatype.restsimple.test.petstore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.restsimple.Web;
import org.sonatype.restsimple.WebDriver;
import org.sonatype.restsimple.api.Action;
import org.sonatype.restsimple.api.DefaultServiceDefinition;
import org.sonatype.restsimple.api.GetServiceHandler;
import org.sonatype.restsimple.api.MediaType;
import org.sonatype.restsimple.api.PostServiceHandler;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.client.ServiceDefinitionClient;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertNotNull;

public class WebTest {

    private static final Logger logger = LoggerFactory.getLogger(WebTest.class);

    private final static MediaType JSON = new MediaType(PetstoreAction.APPLICATION, PetstoreAction.JSON);

    public String targetUrl;

    public String acceptHeader;

    private WebDriver webDriver;

    private ServiceDefinition serviceDefinition;

    private ServiceDefinitionClient stub;

    @BeforeClass(alwaysRun = true)
    public void setUpGlobal() throws Exception {

        acceptHeader = PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON;

        Action action = new PetstoreAction();
        serviceDefinition = new DefaultServiceDefinition();
        serviceDefinition
                .withHandler(new GetServiceHandler("getPet", action).consumeWith(JSON, Pet.class).producing(JSON))
                .withHandler(new PostServiceHandler("addPet", action).consumeWith(JSON, Pet.class).producing(JSON));

        webDriver = WebDriver.getDriver().serviceDefinition(serviceDefinition);
        targetUrl = webDriver.getUri();
        logger.info("Local HTTP server started successfully");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        webDriver.shutdown();
    }

    @Test(timeOut = 20000)
    public void testPost() throws Throwable {
        logger.info("running test: testPut");

        Web web = new Web(serviceDefinition);
        Map<String,String> m = new HashMap<String,String>();
        m.put("Content-Type", acceptHeader);

        Pet pet = (Pet) web.clientOf(targetUrl + "/addPet/myPet").headers(m).post("{\"name\":\"pouetpouet\"}");
        assertNotNull(pet);

        pet = web.clientOf(targetUrl + "/getPet/myPet").headers(m).get(Pet.class);

        assertNotNull(pet);
    }

    @Test(timeOut = 20000)
    public void testPostWithoutSD() throws Throwable {
        logger.info("running test: testPut");

        Web web = new Web();
        Map<String,String> m = new HashMap<String,String>();
        m.put("Content-Type", acceptHeader);
        m.put("Accept", acceptHeader);

        Pet pet = web.clientOf(targetUrl + "/addPet/myPet").headers(m).post("{\"name\":\"pouetpouet\"}", Pet.class);
        assertNotNull(pet);

        pet = web.clientOf(targetUrl + "/getPet/myPet").headers(m).get(Pet.class);

        assertNotNull(pet);
    }

}
