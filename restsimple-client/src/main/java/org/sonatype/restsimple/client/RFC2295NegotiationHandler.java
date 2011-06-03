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
 * A NegotiationHandler based on RFC 2295 http://www.ietf.org/rfc/rfc2295.txt 
 */
public class RFC2295NegotiationHandler implements NegotiationHandler {

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
