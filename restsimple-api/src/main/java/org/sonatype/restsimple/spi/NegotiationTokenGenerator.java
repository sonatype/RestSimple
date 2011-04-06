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
 * Server side implementation for implementing a negotiation challenge between a client and a server.
 */
public interface NegotiationTokenGenerator {

    /**
     * Return the name of the challenged header.
     * @return
     */
    String challengedHeaderName();

    /**
     * Generate an challenge header for challenging the server during version/content negotiation.
     * @param uri The uri challenged
     * @param mediaTypes the list of server's MediaType.
     * @return A string representing the value for the challenged header.
     */
    String generateNegotiationHeader(String uri, List<MediaType> mediaTypes);

}
