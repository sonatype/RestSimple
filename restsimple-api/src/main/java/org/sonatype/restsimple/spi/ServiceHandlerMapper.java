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
import java.util.StringTokenizer;

/**
 * A URI mapper for {@link ServiceHandler}
 */
public class ServiceHandlerMapper {

    private final HashMap<ServiceHandlerInfo, ServiceHandler> maps = new HashMap<ServiceHandlerInfo, ServiceHandler>();

    public ServiceHandlerMapper() {
    }

    /**
     * Ass a {@link ServiceHandler}
     * @param serviceHandler {@link ServiceHandler}
     * @return this
     */
    public ServiceHandlerMapper addServiceHandler(ServiceHandler serviceHandler) {
        maps.put(new ServiceHandlerInfo(serviceHandler.getHttpMethod().name().toLowerCase(), convert(serviceHandler.path())), serviceHandler);
        return this;
    }

    private String convert(String path) {
        if (path.equals("/")) {
            return path;
        }

        StringTokenizer st = new StringTokenizer(path, "/");
        StringBuilder newPath = new StringBuilder();

        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.startsWith(":")) {
                newPath.append("/").append(token.replace(":", "{")).append("}");
            } else {
                newPath.append("/").append(token);
            }
        }
        return newPath.toString();
    }

    /**
     * Remove a {@link ServiceHandler}
     * @param serviceHandler {@link ServiceHandler}
     * @return this
     */
    public ServiceHandlerMapper removeServiceHandler(ServiceHandler serviceHandler) {
        maps.remove(serviceHandler.path());
        return this;
    }

    /**
     * Map the current resource method to its's associated {@link ServiceHandler}
     * @param method The HTTP metod name
     * @param path The current request's path
     * @return a {@link ServiceHandler}, or null if not mapped.
     */
    public ServiceHandler map(String method, String path) {

        // JAXRS remove the / for PathParam, where Sitebricks don't
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        final Map<String, String> m = new HashMap<String, String>();
        for (Map.Entry<ServiceHandlerInfo, ServiceHandler> e : maps.entrySet()) {
            UriTemplate t = new UriTemplate(e.getKey().path);
            if (t.match(path, m) && e.getKey().method.equalsIgnoreCase(method)) {
                return e.getValue();
            }
        }
        return null;
    }

    private final static class ServiceHandlerInfo {

        public final String method;
        public final String path;


        public ServiceHandlerInfo(String method, String path) {
            this.method = method;
            this.path = path;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ServiceHandlerInfo that = (ServiceHandlerInfo) o;

            if (method != null ? !method.equals(that.method) : that.method != null) return false;
            if (path != null ? !path.equals(that.path) : that.path != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = method != null ? method.hashCode() : 0;
            result = 31 * result + (path != null ? path.hashCode() : 0);
            return result;
        }
    }

}
