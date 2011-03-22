/*
 * Copyright (c) 2011 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.restsimple.common.test;

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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class JAXRSModuleClientStubTest {

    private static final Logger logger = LoggerFactory.getLogger(JAXRSModuleClientStubTest.class);

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
        port = 8080;
        server = new Server(port);

        targetUrl = "http://127.0.0.1:" + port;

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.addFilter(GuiceFilter.class, "/*", 0);
        context.addEventListener(new JAXRSModuleConfig());
        context.addServlet(DefaultServlet.class, "/");

        server.setHandler(context);
        server.start();

        Action action = new AddressBookAction();
        serviceDefinition = new DefaultServiceDefinition();

        serviceDefinition
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
        ServiceDefinitionClient stub = ServiceDefinitionProxy.getProxy(targetUrl, serviceDefinition);

        Response r = stub.doPut("myBook");

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 201);
    }

    @Test(timeOut = 20000)
    public void testPost() throws Throwable {
        logger.info("running test: testPost");
        ServiceDefinitionClient stub = ServiceDefinitionProxy.getProxy(targetUrl, serviceDefinition);

        stub.doPut("myBook");
        Map<String,String> m = new HashMap<String,String>();
        m.put("update","foo");
        Response r = stub.doPost(m,"myBook");

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 200);

    }

    @Test(timeOut = 20000)
    public void testGet() throws Throwable {
        logger.info("running test: testGet");
        ServiceDefinitionClient stub = ServiceDefinitionProxy.getProxy(targetUrl, serviceDefinition);

        stub.doPut("myBook");
        Map<String,String> m = new HashMap<String,String>();
        m.put("update","foo");
        stub.doPost(m,"myBook");
        Response r = stub.doGet("myBook");

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 200);
        System.out.println(r.getResponseBody());
        assertEquals(r.getResponseBody(), "{\"entries\":\"foo - \"}");
    }

    @Test(timeOut = 20000)
    public void testDelete() throws Throwable {
        logger.info("running test: testDelete");
        ServiceDefinitionClient stub = ServiceDefinitionProxy.getProxy(targetUrl, serviceDefinition);

        stub.doPut("myBook");
        Map<String,String> m = new HashMap<String,String>();
        m.put("update","foo");
        Response r = stub.doPost(m,"myBook");
        assertEquals(r.getStatusCode(), 200);

        stub.doDelete("myBook");
        r = stub.doGet("myBook");

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 500);
    }

}