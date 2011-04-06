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
package org.sonatype.restsimple.client;

import com.sun.jersey.api.client.ClientResponse;
import org.sonatype.restsimple.api.MediaType;

import java.util.List;

/**
 * Implementation of this class implements the necessary handling for content and version negotiation
 */
public interface NegotiateHandler {

    /**
     * Decides if it is possible to challenge the server from a set of response's headers.
     *
     * @param mediaTypes  a List of supported media type.
     * @param response the response's instance
     * @return a new Accept header value to use for challenging the server
     * @throws WebException if no challenge is possible.
     */
    public String negotiate(List<MediaType> mediaTypes, ClientResponse response) throws WebException;

}
