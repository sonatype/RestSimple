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

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.restsimple.WebDriver;
import org.sonatype.restsimple.api.Action;
import org.sonatype.restsimple.api.DefaultServiceDefinition;
import org.sonatype.restsimple.api.GetServiceHandler;
import org.sonatype.restsimple.api.MediaType;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.example.hello.HelloWorldAction;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class HelloWorldTest {

    private static final Logger logger = LoggerFactory.getLogger(HelloWorldTest.class);

    public String targetUrl;

    public String acceptHeader;

    private WebDriver webDriver;

    private ServiceDefinition serviceDefinition;

    @BeforeClass(alwaysRun = true)
    public void setUpGlobal() throws Exception {

        acceptHeader = HelloWorldAction.APPLICATION + "/" + HelloWorldAction.JSON;

        Action action = new HelloWorldAction();
        ServiceDefinition serviceDefinition = new DefaultServiceDefinition();
        serviceDefinition
                .withHandler(new GetServiceHandler("sayPlainTextHello", action).producing(
                        new MediaType(HelloWorldAction.APPLICATION, HelloWorldAction.TXT)))
                .withHandler(new GetServiceHandler("sayPlainXmlHello", action).producing(
                        new MediaType(HelloWorldAction.APPLICATION, HelloWorldAction.XML)))
                .withHandler(new GetServiceHandler("sayPlainHtmlHello", action).producing(
                        new MediaType(HelloWorldAction.APPLICATION, HelloWorldAction.HTML)))
                .withHandler(new GetServiceHandler("sayPlainJsonHello", action).producing(
                        new MediaType(HelloWorldAction.APPLICATION, HelloWorldAction.JSON)));

        webDriver = WebDriver.getDriver().serviceDefinition(serviceDefinition);
        targetUrl = webDriver.getUri();
        logger.info("Local HTTP server started successfully");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        webDriver.shutdown();
    }

    @Test(timeOut = 20000)
    public void testGetPlainText() throws Throwable {
        logger.info("running test: testGetPlainText");
        AsyncHttpClient c = new AsyncHttpClient();
        acceptHeader = HelloWorldAction.APPLICATION + "/" + HelloWorldAction.TXT;
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
        acceptHeader = HelloWorldAction.APPLICATION + "/" + HelloWorldAction.HTML;

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
        acceptHeader = HelloWorldAction.APPLICATION + "/" + HelloWorldAction.XML;

        Response r = c.prepareGet(targetUrl + "/sayPlainXmlHello/sonatype").addHeader("Accept", acceptHeader).execute().get();

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 200);
        System.out.println(r.getResponseBody());
        assertEquals(r.getResponseBody(), "<org.sonatype.restsimple.example.hello.HelloWorldMediaType>\n" +
                "  <helloWorld>sonatype</helloWorld>\n" +
                "</org.sonatype.restsimple.example.hello.HelloWorldMediaType>");

        c.close();
    }

    @Test(timeOut = 20000)
    public void testGetJSON() throws Throwable {
        logger.info("running test: testGetJSON");
        AsyncHttpClient c = new AsyncHttpClient();
        acceptHeader = HelloWorldAction.APPLICATION + "/" + HelloWorldAction.JSON;

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
        acceptHeader = HelloWorldAction.APPLICATION + "/" + "vnd.org.sonatype.rest-v2+json";

        Response r = c.prepareGet(targetUrl + "/sayPlainJsonHello/sonatype").addHeader("Accept", acceptHeader).execute().get();

        assertNotNull(r);
        assertEquals(r.getStatusCode(), 406);
        c.close();
    }
}