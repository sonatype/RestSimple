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
