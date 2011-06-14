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

import com.google.inject.ImplementedBy;
import org.sonatype.restsimple.api.MediaType;

import java.util.List;

/**
 * Server side component for implementing a version or content negotiation challenge between a client and a server.
 */
@ImplementedBy( RFC2295NegotiationTokenGenerator.class )
public interface NegotiationTokenGenerator {

    /**
     * Return the name of the challenged header.
     * @return he name of the challenged header.
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
