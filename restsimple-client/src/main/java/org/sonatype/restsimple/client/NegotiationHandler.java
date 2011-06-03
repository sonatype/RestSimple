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
package org.sonatype.restsimple.client;

import org.sonatype.restsimple.api.MediaType;

import java.util.List;
import java.util.Map;

/**
 * Implementation of this class implements the handling logic for content and version negotiation
 */
public interface NegotiationHandler {

    /**
     * Return the name of the challenged header.
     *
     * @return the name of the challenged header.
     */
    String challengedHeaderName();

    /**
     * Decides if it is possible to challenge the server from a set of response's headers.
     *
     * @param mediaTypes   a List of supported media type.
     * @param headers      the response's headers
     * @param statusCode   the response's status code
     * @param reasonPhrase the response's reason phrase
     * @return a new Accept header value to use for challenging the server
     * @throws WebException if no challenge is possible.
     */
    String negotiate(List<MediaType> mediaTypes, Map<String, List<String>> headers, int statusCode, String reasonPhrase) throws WebException;

}
