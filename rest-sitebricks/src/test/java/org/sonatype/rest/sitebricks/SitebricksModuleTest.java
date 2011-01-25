/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.sonatype.rest.sitebricks;

import com.google.inject.servlet.GuiceFilter;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class SitebricksModuleTest {

    private static final Logger logger = LoggerFactory.getLogger(SitebricksModuleTest.class);

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
        acceptHeader = AddressBookServiceEntity.APPLICATION + "/" + AddressBookServiceEntity.JSON;

        targetUrl = "http://127.0.0.1:" + port;

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.addFilter(GuiceFilter.class, "/*", 0);
        context.addEventListener(new SitebricksModuleConfig());
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
        Response r = c.preparePost(targetUrl + "/updateAddressBook/myBook").addHeader("Accept", acceptHeader).addParameter("update","foo").execute().get();

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 200);

        c.close();
    }

    @Test(timeOut = 20000)
    public void testInvalidPost() throws Throwable {
        logger.info("running test: testInvalidPost");
        AsyncHttpClient c = new AsyncHttpClient();

        Response r = c.preparePost(targetUrl + "/createAddressBook/myBook").addHeader("Accept", acceptHeader).addParameter("update","foo").execute().get();

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 405);

        c.close();
    }

    @Test(timeOut = 20000)
    public void testGet() throws Throwable {
        logger.info("running test: testGet");
        AsyncHttpClient c = new AsyncHttpClient();

        c.preparePut(targetUrl + "/createAddressBook/myBook").addHeader("Accept", acceptHeader).execute().get();
        c.preparePost(targetUrl + "/updateAddressBook/myBook").addHeader("Accept", acceptHeader).addParameter("update","foo").execute().get();
        Response r = c.prepareGet(targetUrl + "/getAddressBook/myBook").addHeader("Accept", acceptHeader).execute().get();

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 200);
        System.out.println(r.getResponseBody());
        assertEquals(r.getResponseBody(), "{\"entries\":\"foo - \"}");

        c.close();
    }

    @Test(timeOut = 20000)
    public void testInvalidBookGet() throws Throwable {
        logger.info("running test: testInvalidBookGet");
        AsyncHttpClient c = new AsyncHttpClient();

        Response r = c.prepareGet(targetUrl + "/getAddressBook/zeBook").addHeader("Accept", acceptHeader).execute().get();

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 200);
        System.out.println(r.getResponseBody());
        assertEquals(r.getResponseBody(), "{\"entries\":\"\"}");

        c.close();
    }

    @Test(timeOut = 20000)
    public void testDelete() throws Throwable {
        logger.info("running test: testGet");
        AsyncHttpClient c = new AsyncHttpClient();

        c.preparePut(targetUrl + "/createAddressBook/myBook").addHeader("Accept", acceptHeader).execute().get();
        c.preparePost(targetUrl + "/updateAddressBook/myBook").addHeader("Accept", acceptHeader).addParameter("update","foo").execute().get();
        c.prepareDelete(targetUrl + "/deleteAddressBook/myBook").addHeader("Accept", acceptHeader).execute().get();
        Response r = c.prepareGet(targetUrl + "/getAddressBook/myBook").addHeader("Accept", acceptHeader).execute().get();

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 200);
        System.out.println(r.getResponseBody());
        assertEquals(r.getResponseBody(), "{\"entries\":\"\"}");

        c.close();
    }

    @Test(timeOut = 20000, enabled = false)
    public void testInvalidAcceptPut() throws Throwable {
        logger.info("running test: testInvalidAcceptPut");
        AsyncHttpClient c = new AsyncHttpClient();

        Response r = c.preparePut(targetUrl + "/createAddressBook/myBook").addHeader("Accept", "foo").execute().get();

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 406);

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
}