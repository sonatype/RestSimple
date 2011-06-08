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
package org.sonatype.restsimple.spi;

import org.sonatype.restsimple.api.MediaType;

import java.util.List;

/**
 * Generate an <tt>Alternates</tt> headers based on RFC 2295.
 */
public class RFC2295NegotiationTokenGenerator implements NegotiationTokenGenerator {

    /**
     * {@inheritDoc}
     */
    @Override
    public String challengedHeaderName() {
        return "Alternates";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateNegotiationHeader(String uri, List<MediaType> mediaTypes) {
        StringBuilder acceptedContentType = new StringBuilder();
        for (MediaType mediaType : mediaTypes) {
            acceptedContentType.append("{\"")
                    .append(uri)
                    .append("\" 1.0 ")
                    .append("{type ")
                    .append(mediaType.toMediaType())
                    .append("}},");
        }
        if (acceptedContentType.length() > 0) {
            acceptedContentType.delete(acceptedContentType.length() - 1, acceptedContentType.length());
        }
        return acceptedContentType.toString();
    }
}
