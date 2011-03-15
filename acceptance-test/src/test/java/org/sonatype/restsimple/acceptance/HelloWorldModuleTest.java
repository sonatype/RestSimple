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
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.restsimple.example.hello.HelloWorldModuleConfig;
import org.sonatype.restsimple.example.hello.HelloWorldAction;
import org.sonatype.restsimple.common.test.AddressBookAction;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class HelloWorldModuleTest {

    private static final Logger logger = LoggerFactory.getLogger(HelloWorldModuleTest.class);

    protected Server server;

    public int port;

    public String targetUrl;
    
    public String acceptHeader;

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
        context.addEventListener(new HelloWorldModuleConfig());
        context.addServlet(DefaultServlet.class, "/");

        server.setHandler(context);
        server.start();
        logger.info("Local HTTP server started successfully");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test(timeOut = 20000)
    public void testGetPlainText() throws Throwable {
        logger.info("running test: testGetPlainText");
        AsyncHttpClient c = new AsyncHttpClient();
        acceptHeader = AddressBookAction.APPLICATION + "/" + HelloWorldAction.TXT;
        Response r = c.prepareGet(targetUrl + "/sayPlainTextHello/sonatype").addHeader("Accept", acceptHeader).execute().get();

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 200);
        System.out.println(r.getResponseBody());
        assertEquals(r.getResponseBody(), "Hello RestSimple sonatype");

        c.close();
    }

    @Test(timeOut = 20000)
    public void testGetHTML() throws Throwable {
        logger.info("running test: testGetHTML");
        AsyncHttpClient c = new AsyncHttpClient();
        acceptHeader = AddressBookAction.APPLICATION + "/" + HelloWorldAction.HTML;

        Response r = c.prepareGet(targetUrl + "/sayPlainHtmlHello/sonatype").addHeader("Accept", acceptHeader).execute().get();

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 200);
        System.out.println(r.getResponseBody());
        assertEquals(r.getResponseBody(), "<html> " + "<title>" + "Hello RestSimple sonatype</title>"
                + "<body><h1>" + "Hello Jersey" + "</body></h1>" + "</html> ");

        c.close();
    }

    @Test(timeOut = 20000)
    public void testGetXML() throws Throwable {
        logger.info("running test: testGetXML");
        AsyncHttpClient c = new AsyncHttpClient();
        acceptHeader = AddressBookAction.APPLICATION + "/" + HelloWorldAction.XML;

        Response r = c.prepareGet(targetUrl + "/sayPlainXmlHello/sonatype").addHeader("Accept", acceptHeader).execute().get();

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 200);
        System.out.println(r.getResponseBody());
        assertEquals(r.getResponseBody(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><helloWorldMediaType><helloWorld>sonatype</helloWorld></helloWorldMediaType>");

        c.close();
    }

    @Test(timeOut = 20000)
    public void testGetJSON() throws Throwable {
        logger.info("running test: testGetJSON");
        AsyncHttpClient c = new AsyncHttpClient();
        acceptHeader = AddressBookAction.APPLICATION + "/" + HelloWorldAction.JSON;

        Response r = c.prepareGet(targetUrl + "/sayPlainJsonHello/sonatype").addHeader("Accept", acceptHeader).execute().get();

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 200);
        System.out.println(r.getResponseBody());
        assertEquals(r.getResponseBody(), "{\"helloWorld\":\"sonatype\"}");

        c.close();
    }

    @Test(timeOut = 20000)
    public void testContentNegociation() throws Throwable {
        logger.info("running test: testContentNegociation");
        AsyncHttpClient c = new AsyncHttpClient();
        acceptHeader = AddressBookAction.APPLICATION + "/" + "vnd.org.sonatype.rest-v2+json";

        Response r = c.prepareGet(targetUrl + "/sayPlainJsonHello/sonatype").addHeader("Accept", acceptHeader).execute().get();

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 406);
        c.close();
    }
}