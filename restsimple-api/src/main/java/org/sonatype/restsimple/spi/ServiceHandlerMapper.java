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

import org.sonatype.restsimple.api.ServiceHandler;
import org.sonatype.restsimple.spi.uri.UriTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple class generated resource can you to map the request to its associated {@link ServiceHandler} representation.
 */
public class ServiceHandlerMapper {

    private final HashMap<String, ServiceHandler> maps = new HashMap<String, ServiceHandler>();

    private boolean supportWildcard;

    public ServiceHandlerMapper() {
    }

    /**
     * Ass a {@link ServiceHandler}
     * @param serviceHandler {@link ServiceHandler}
     * @return this
     */
    public ServiceHandlerMapper addServiceHandler(ServiceHandler serviceHandler) {
        maps.put(serviceHandler.getPath(), serviceHandler);
        return this;
    }

    /**
     * Remove a {@link ServiceHandler}
     * @param serviceHandler {@link ServiceHandler}
     * @return this
     */
    public ServiceHandlerMapper removeServiceHandler(ServiceHandler serviceHandler) {
        maps.remove(serviceHandler.getPath());
        return this;
    }

    /**
     * Map the current resource method to its's associated {@link ServiceHandler}
     * @param path The current request's path
     * @return a {@link ServiceHandler}, or null if not mapped.
     */
    public ServiceHandler map(String path) {
        final Map<String, String> m = new HashMap<String, String>();
        for (Map.Entry<String, ServiceHandler> e : maps.entrySet()) {
            UriTemplate t = new UriTemplate(e.getKey());
            if (t.match(path, m)) {
                return e.getValue();
            }
        }
        return null;
    }

}
