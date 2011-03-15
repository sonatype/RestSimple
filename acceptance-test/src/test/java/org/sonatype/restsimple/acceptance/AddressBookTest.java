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

import com.google.inject.servlet.GuiceFilter;
import com.ning.http.client.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.restsimple.api.Action;
import org.sonatype.restsimple.api.DefaultServiceDefinition;
import org.sonatype.restsimple.api.DeleteServiceHandler;
import org.sonatype.restsimple.api.GetServiceHandler;
import org.sonatype.restsimple.api.MediaType;
import org.sonatype.restsimple.api.PostServiceHandler;
import org.sonatype.restsimple.api.PutServiceHandler;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.client.ServiceDefinitionClient;
import org.sonatype.restsimple.client.ServiceDefinitionProxy;
import org.sonatype.restsimple.common.test.AddressBookAction;
import org.sonatype.restsimple.example.addressBook.AddressBookModuleConfig;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class AddressBookTest {

    private static final Logger logger = LoggerFactory.getLogger(AddressBookTest.class);

    protected Server server;

    public int port;

    public String targetUrl;

    private ServiceDefinition serviceDefinition;

    protected int findFreePort() throws IOException {
        ServerSocket socket = null;

        try {
            socket = new ServerSocket(0);

            return socket.getLocalPort();
        }
        finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    @BeforeClass(alwaysRun = true)
    public void setUpGlobal() throws Exception {
        port = findFreePort();
        server = new Server(port);

        targetUrl = "http://127.0.0.1:" + port;

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.addFilter(GuiceFilter.class, "/*", 0);
        context.addEventListener(new AddressBookModuleConfig());
        context.addServlet(DefaultServlet.class, "/");

        server.setHandler(context);
        server.start();

        Action action = new AddressBookAction();
        serviceDefinition = new DefaultServiceDefinition();
        serviceDefinition.withPath(targetUrl)
                .producing(new MediaType(AddressBookAction.APPLICATION, AddressBookAction.JSON))
                .producing(new MediaType(AddressBookAction.APPLICATION, AddressBookAction.XML))
                .consuming(MediaType.JSON)
                .consuming(MediaType.XML)
                .withHandler(new PutServiceHandler("createAddressBook", action))
                .withHandler(new GetServiceHandler("getAddressBook", action))
                .withHandler(new PostServiceHandler("updateAddressBook", action))
                .withHandler(new DeleteServiceHandler("deleteAddressBook", action));


        logger.info("Local HTTP server started successfully");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test(timeOut = 20000)
    public void testPut() throws Throwable {
        logger.info("running test: testPut");
        ServiceDefinitionClient stub = ServiceDefinitionProxy.getProxy(serviceDefinition);

        Response r = stub.doPut("myBook");

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 201);
    }

    @Test(timeOut = 20000)
    public void testPost() throws Throwable {
        logger.info("running test: testPost");
        ServiceDefinitionClient stub = ServiceDefinitionProxy.getProxy(serviceDefinition);

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
        ServiceDefinitionClient stub = ServiceDefinitionProxy.getProxy(serviceDefinition);

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
        ServiceDefinitionClient stub = ServiceDefinitionProxy.getProxy(serviceDefinition);

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