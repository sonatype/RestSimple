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