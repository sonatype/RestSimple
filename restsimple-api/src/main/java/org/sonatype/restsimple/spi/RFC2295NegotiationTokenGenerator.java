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
package org.sonatype.restsimple.spi;

import org.sonatype.restsimple.api.MediaType;

import java.util.List;

/**
 * Generate an Alternates headers based on RFC 2295.
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
