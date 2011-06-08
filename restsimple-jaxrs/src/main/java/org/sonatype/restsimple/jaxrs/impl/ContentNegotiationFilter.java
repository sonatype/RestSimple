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
package org.sonatype.restsimple.jaxrs.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.sonatype.restsimple.api.ServiceHandler;
import org.sonatype.restsimple.spi.NegotiationTokenGenerator;
import org.sonatype.restsimple.spi.ServiceHandlerMapper;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

@Singleton
public class ContentNegotiationFilter implements Filter {

    private final ServiceHandlerMapper mapper;

    private final NegotiationTokenGenerator negotiationTokenGenerator;

    private final Logger logger;

    @Inject
    public ContentNegotiationFilter(ServiceHandlerMapper mapper, NegotiationTokenGenerator negotiationTokenGenerator, Logger logger) {
        this.mapper = mapper;
        this.negotiationTokenGenerator = negotiationTokenGenerator;
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest hreq = HttpServletRequest.class.cast(request);
        String[] paths = hreq.getServletPath().split("/");

        String pathName;
        if (paths.length > 0) {
            pathName = paths[1];
        } else {
            pathName = "";
        }

        ServiceHandler serviceHandler = mapper.map(hreq.getMethod(), pathName);
        logger.info("Configuring Negotiation Token Header to " + pathName + " with ServiceHandler " + serviceHandler);

        // TODO: Must add a special header in Jersey generation so we don't add the header for all request.
        if (serviceHandler != null) {
            HttpServletResponse hres = HttpServletResponse.class.cast(response);
            hres.addHeader(negotiationTokenGenerator.challengedHeaderName(),
                           negotiationTokenGenerator.generateNegotiationHeader(pathName, serviceHandler.mediaToProduce()));
        }
        chain.doFilter(request, response);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
    }
}
