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
package org.sonatype.restsimple.api;

import java.util.List;
import java.util.Map;

/**
 * Implementation of this class implements the necessary handling for content and version negotiation
 */
public interface NegotiationHandler {

    /**
     * Return the name of the challenged header.
     *
     * @return
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
    public String negotiate(List<MediaType> mediaTypes, Map<String, List<String>> headers, int statusCode, String reasonPhrase) throws WebException;

}
