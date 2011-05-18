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
package org.sonatype.restsimple;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.api.ServiceHandler;
import org.sonatype.restsimple.jaxrs.guice.JaxrsModule;
import org.sonatype.restsimple.jaxrs.impl.ContentNegotiationFilter;
import org.sonatype.restsimple.jaxrs.impl.JAXRSServiceDefinitionGenerator;
import org.sonatype.restsimple.sitebricks.guice.RestSimpleSitebricksModule;
import org.sonatype.restsimple.sitebricks.impl.SitebricksServiceDefinitionGenerator;
import org.sonatype.restsimple.spi.ServiceHandlerMapper;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;

/**
 * Simple utilities class that configure Jetty and properly bind a {@link ServiceDefinition} to
 * its {@link org.sonatype.restsimple.spi.ServiceDefinitionGenerator}. The purpose of this class is to easy unit test
 * development.
 */
public class WebDriver {

    public static enum PROVIDER {
        JAXRS, SITEBRICKS
    }

    private static final Logger logger = LoggerFactory.getLogger(WebDriver.class);

    private final Server server;

    private final ServletContextHandler context;

    private final String targetUrl;

    private PROVIDER provider = PROVIDER.JAXRS;

    private WebDriver(Server server, String targetUrl, ServletContextHandler context) {
        this.server = server;
        this.targetUrl = targetUrl;
        this.context = context;
    }

    private static int findFreePort() throws IOException {
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

    public static WebDriver getDriver() throws Exception {
        return getDriver(PROVIDER.SITEBRICKS);
    }

    public static WebDriver getDriver(PROVIDER provider) throws Exception {

        int port = findFreePort();
        String targetUrl = "http://127.0.0.1:" + port;
        Server server = new org.eclipse.jetty.server.Server(port);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.addFilter(GuiceFilter.class, "/*", 0);
        context.addServlet(DefaultServlet.class, "/");
        server.setHandler(context);

        WebDriver w = new WebDriver(server, targetUrl, context);
        w.provider = provider;
        return w;
    }

    public WebDriver shutdown() throws Exception {
        server.stop();
        return this;
    }

    public String getUri() {
        return targetUrl;
    }

    public WebDriver serviceDefinition(final ServiceDefinition serviceDefinition) throws Exception {

        context.addEventListener(new GuiceServletContextListener() {
            @Override
            protected Injector getInjector() {
                return Guice.createInjector(new ServletModule() {
                    @Override
                    public void configureServlets() {
                        Injector injector;
                        if (provider.equals(PROVIDER.JAXRS)) {
                            injector = Guice.createInjector(new JaxrsModule(binder().withSource("[generated]")));
                        } else {
                            injector = Guice.createInjector(new RestSimpleSitebricksModule(binder()));
                        }

                        // If the ServiceDefinition was created without using injection, we need to get the proper
                        // list of ServiceHandler as more than one instance of ServiceHandlerMapper exist
                        ServiceHandlerMapper mapper = injector.getInstance(ServiceHandlerMapper.class);
                        for (ServiceHandler handler : serviceDefinition.serviceHandlers()) {
                            mapper.addServiceHandler(serviceDefinition.path(), handler);
                        }

                        if (provider.equals(PROVIDER.JAXRS)) {
                            injector.getInstance(JAXRSServiceDefinitionGenerator.class).generate(serviceDefinition);
                            HashMap<String, String> initParams = new HashMap<String, String>();
                            initParams.put("com.sun.jersey.api.json.POJOMappingFeature", "true");
                            //initParams.put("com.sun.jersey.config.feature.Trace", "true");
                            
                            filter("/*").through(ContentNegotiationFilter.class);
                            serve("/*").with(GuiceContainer.class, initParams);
                        } else {
                            injector.getInstance(SitebricksServiceDefinitionGenerator.class).generate(serviceDefinition);
                        }
                    }
                });
            }
        });

        server.start();
        return this;
    }
}
