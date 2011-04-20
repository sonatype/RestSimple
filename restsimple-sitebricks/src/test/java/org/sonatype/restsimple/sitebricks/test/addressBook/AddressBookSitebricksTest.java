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
package org.sonatype.restsimple.sitebricks.test.addressBook;

import com.google.inject.servlet.GuiceFilter;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.restsimple.common.test.addressbook.AddressBookAction;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class AddressBookSitebricksTest {

    private static final Logger logger = LoggerFactory.getLogger(AddressBookSitebricksTest.class);

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
        acceptHeader = AddressBookAction.APPLICATION + "/" + AddressBookAction.JSON;

        targetUrl = "http://127.0.0.1:" + port;

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.addFilter(GuiceFilter.class, "/*", 0);
        context.addEventListener(new AddressBookSitebricksConfig());
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
    public void testPut() throws Throwable {
        logger.info("running test: testPut");
        AsyncHttpClient c = new AsyncHttpClient();

        Response r = c.preparePut(targetUrl + "/createAddressBook/myBook").addHeader("Accept", acceptHeader).execute().get();

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 201);

        c.close();
    }

    @Test(timeOut = 20000)
    public void testPost() throws Throwable {
        logger.info("running test: testPost");
        AsyncHttpClient c = new AsyncHttpClient();

        c.preparePut(targetUrl + "/createAddressBook/myBook").addHeader("Accept", acceptHeader).execute().get();
        Response r = c.preparePost(targetUrl + "/updateAddressBook/myBook").addHeader("Accept", acceptHeader).addParameter("update","foo").addParameter("update2", "bar").execute().get();

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 200);

        c.close();
    }

    @Test(timeOut = 20000)
    public void testInvalidPost() throws Throwable {
        logger.info("running test: testInvalidPost");
        AsyncHttpClient c = new AsyncHttpClient();

        Response r = c.preparePost(targetUrl + "/createAddressBook/myBook").addHeader("Accept", acceptHeader).addParameter("update","foo").addParameter("update2", "bar").execute().get();

        assertNotNull(r);
        // TODO: Must return 406, not 500 -> Sitebricks exception
        try {
            assertEquals(r.getStatusCode(), 406);
        } catch (Throwable t) {
            assertEquals(r.getStatusCode(), 500);
        }

        c.close();
    }

    @Test(timeOut = 20000)
    public void testGet() throws Throwable {
        logger.info("running test: testGet");
        AsyncHttpClient c = new AsyncHttpClient();

        c.preparePut(targetUrl + "/createAddressBook/myBook").addHeader("Accept", acceptHeader).execute().get();
        c.preparePost(targetUrl + "/updateAddressBook/myBook").addHeader("Accept", acceptHeader).addParameter("update","foo").addParameter("update2", "bar").execute().get();
        Response r = c.prepareGet(targetUrl + "/getAddressBook/myBook").addHeader("Accept", acceptHeader).execute().get();

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 200);
        System.out.println(r.getResponseBody());
        assertEquals(r.getResponseBody(), "{\"entries\":\"foo - bar - \"}");

        c.close();
    }

    @Test(timeOut = 20000)
    public void testInvalidBookGet() throws Throwable {
        logger.info("running test: testInvalidBookGet");
        AsyncHttpClient c = new AsyncHttpClient();

        Response r = c.prepareGet(targetUrl + "/getAddressBook/zeBook").addHeader("Accept", acceptHeader).execute().get();

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 500);

        c.close();
    }

    @Test(timeOut = 20000)
    public void testDelete() throws Throwable {
        logger.info("running test: testGet");
        AsyncHttpClient c = new AsyncHttpClient();

        c.preparePut(targetUrl + "/createAddressBook/myBook").addHeader("Accept", acceptHeader).execute().get();
        Response r = c.preparePost(targetUrl + "/updateAddressBook/myBook").addHeader("Accept", acceptHeader).addParameter("update","foo").addParameter("update2", "bar").execute().get();
        assertEquals(r.getStatusCode(), 200);
        c.prepareDelete(targetUrl + "/deleteAddressBook/myBook").addHeader("Accept", acceptHeader).execute().get();
        r = c.prepareGet(targetUrl + "/getAddressBook/myBook").addHeader("Accept", acceptHeader).execute().get();

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 500);

        c.close();
    }

    @Test(timeOut = 20000, enabled = true)
    public void testInvalidAcceptPut() throws Throwable {
        logger.info("running test: testInvalidAcceptPut");
        AsyncHttpClient c = new AsyncHttpClient();

        Response r = c.preparePut(targetUrl + "/createAddressBook/myBook").addHeader("Accept", "foo").execute().get();

        assertNotNull(r);
        // TODO: Must return 406, not 500 -> Sitebricks exception
        try {
            assertEquals(r.getStatusCode(), 406);
        } catch (Throwable t) {
            assertEquals(r.getStatusCode(), 500);
        }

        c.close();
    }

    @Test(timeOut = 20000)
    public void testAcceptPut() throws Throwable {
        logger.info("running test: testAcceptPut");
        AsyncHttpClient c = new AsyncHttpClient();

        Response r = c.preparePut(targetUrl + "/createAddressBook/myBook").addHeader("Accept", acceptHeader).execute().get();

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 201);

        c.close();
    }

    @Test(timeOut = 20000, enabled = true)
    public void testSecondResourceGet() throws Throwable {
        logger.info("running test: testSecondResourceGet");
        AsyncHttpClient c = new AsyncHttpClient();

        c.preparePut(targetUrl + "/foo/createAddressBook/myBook").addHeader("Accept", acceptHeader).execute().get();
        Response r = c.preparePost(targetUrl + "/foo/updateAddressBook/myBook").addHeader("Accept", acceptHeader).addParameter("update","foo").addParameter("update2", "bar").execute().get();
        assertEquals(r.getStatusCode(), 200);

        r = c.prepareGet(targetUrl + "/foo/getAddressBook/myBook").addHeader("Accept", acceptHeader).execute().get();

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 200);
        System.out.println(r.getResponseBody());
        assertEquals(r.getResponseBody(), "{\"entries\":\"foo - bar - \"}");

        c.close();
    }

    @Test(timeOut = 20000, enabled = true)
    public void testGetWithPostBody() throws Throwable {
        logger.info("running test: testGetWithPostBody");
        AsyncHttpClient c = new AsyncHttpClient();

        c.preparePut(targetUrl + "/createAddressBook/myBook").addHeader("Accept", acceptHeader).execute().get();
        c.preparePost(targetUrl + "/updateAddressBook/myBook").addHeader("Accept", acceptHeader).addParameter("update","foo").addParameter("update2", "bar").execute().get();
        Response r = c.prepareGet(targetUrl + "/getAddressBook/myBook").addHeader("Accept", acceptHeader).execute().get();

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 200);
        System.out.println(r.getResponseBody());
        assertEquals(r.getResponseBody(), "{\"entries\":\"foo - bar - \"}");

        c.close();
    }
}