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

import org.sonatype.restsimple.api.MediaType;

import java.util.List;
import java.util.Map;

/**
 * A NegotiationHandler based on RFC 2295 http://www.ietf.org/rfc/rfc2295.txt 
 */
public class RFC2295NegotiateHandler implements NegotiateHandler {

    @Override
    public String challengedHeaderName() {
        return "Accept";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String negotiate(List<MediaType> mediaTypes, Map<String,List<String>> headers, int statusCode, String reasonPhrase) throws WebException {
        List<String> list = headers.get("Alternates");
        if ( list != null && statusCode == 406 && mediaTypes.size() > 0) {
            String[] serverChallenge = list.get(0).split(",");
            for (String challenge : serverChallenge) {
                int typePos = challenge.indexOf("type");
                int eof = challenge.indexOf("}", typePos);
                if (typePos > 0) {
                    String type = challenge.substring(typePos + "type".length(), eof).trim();
                    for (MediaType m: mediaTypes) {
                        if (type.equalsIgnoreCase(m.toMediaType())) {
                            return type;
                        }
                    }
                }
            }
        }
        throw new WebException(statusCode, reasonPhrase);
    }
}
