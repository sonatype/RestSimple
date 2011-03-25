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
package org.sonatype.restsimple.acceptance;

import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.restsimple.WebDriver;
import org.sonatype.restsimple.api.Action;
import org.sonatype.restsimple.api.DefaultServiceDefinition;
import org.sonatype.restsimple.api.DeleteServiceHandler;
import org.sonatype.restsimple.api.GetServiceHandler;
import org.sonatype.restsimple.api.MediaType;
import org.sonatype.restsimple.api.PostServiceHandler;
import org.sonatype.restsimple.api.PutServiceHandler;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.client.ServiceDefinitionClient;
import org.sonatype.restsimple.common.test.AddressBookAction;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class AddressBookTest {

    private static final Logger logger = LoggerFactory.getLogger(AddressBookTest.class);

    private WebDriver webDriver;

    private ServiceDefinition serviceDefinition;

    private ServiceDefinitionClient stub;

    @BeforeClass(alwaysRun = true)
    public void setUpGlobal() throws Exception {

        Action action = new AddressBookAction();
        PostServiceHandler postServiceHandler = new PostServiceHandler("updateAddressBook", action);
        postServiceHandler.addFormParam("update");
        postServiceHandler.addFormParam("update2");

        serviceDefinition = new DefaultServiceDefinition();        
        serviceDefinition
                .withPath("")
                .producing(new MediaType(AddressBookAction.APPLICATION, AddressBookAction.JSON))
                .producing(new MediaType(AddressBookAction.APPLICATION, AddressBookAction.XML))
                .consuming(new MediaType(AddressBookAction.APPLICATION, AddressBookAction.JSON))
                .withHandler(new PutServiceHandler("createAddressBook", action))
                .withHandler(new GetServiceHandler("getAddressBook", action))
                .withHandler(postServiceHandler)
                .withHandler(new DeleteServiceHandler("deleteAddressBook", action));

        webDriver = WebDriver.getDriver().serviceDefinition(serviceDefinition);
        stub = webDriver.stub();

        logger.info("Local HTTP server started successfully");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        webDriver.shutdown();
    }

    @Test(timeOut = 20000)
    public void testPut() throws Throwable {
        logger.info("running test: testPut");
        Response r = stub.doPut("myBook");

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 201);
    }

    @Test(timeOut = 20000)
    public void testPost() throws Throwable {
        logger.info("running test: testPost");
        stub.doPut("myBook");
        Map<String, String> m = new HashMap<String, String>();
        m.put("update", "foo");
        Response r = stub.doPost(m, "myBook");

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 200);

    }

    @Test(timeOut = 20000)
    public void testGet() throws Throwable {
        logger.info("running test: testGet");
        stub.doPut("myBook");
        Map<String, String> m = new HashMap<String, String>();
        m.put("update", "foo");
        stub.doPost(m, "myBook");
        Response r = stub.doGet("myBook");

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 200);
        System.out.println(r.getResponseBody());
        assertEquals(r.getResponseBody(), "{\"entries\":\"foo - \"}");
    }

    @Test(timeOut = 20000)
    public void testDelete() throws Throwable {
        logger.info("running test: testDelete");
        stub.doPut("myBook");
        Map<String, String> m = new HashMap<String, String>();
        m.put("update", "foo");
        Response r = stub.doPost(m, "myBook");
        assertEquals(r.getStatusCode(), 200);

        stub.doDelete("myBook");
        r = stub.doGet("myBook");

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 500);
    }

}