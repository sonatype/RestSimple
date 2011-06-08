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

import com.google.inject.Singleton;
import org.sonatype.restsimple.api.ServiceHandler;
import org.sonatype.restsimple.spi.uri.UriTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * A URI mapper for {@link ServiceHandler}. It is recommended to let Guice inject instance of that class. Having multiple
 * instance can cause mapping issue (not found) if the wrong instance of that class get used.
 */
@Singleton
public class ServiceHandlerMapper {

    private final HashMap<ServiceHandlerInfo, ServiceHandler> maps = new HashMap<ServiceHandlerInfo, ServiceHandler>();

    public ServiceHandlerMapper() {
    }

    /**
     * Create a ServiceMapper using the list of {@link ServiceHandler}
     * @param serviceHandlers the list of {@link ServiceHandler}
     */
    public ServiceHandlerMapper(List<ServiceHandler> serviceHandlers) {
        for (ServiceHandler s: serviceHandlers) {
            addServiceHandler("", s);
        }
    }

    /**
     * Add a {@link ServiceHandler}
     * @param serviceHandler {@link ServiceHandler}
     * @return this
     */
    public ServiceHandlerMapper addServiceHandler(String path, ServiceHandler serviceHandler) {
        String realPath = serviceHandler.path();
        if (!path.equals("")) {
            realPath = path + (serviceHandler.path().startsWith("/") ? serviceHandler.path() : "/" + serviceHandler.path());
        }
        
        maps.put(new ServiceHandlerInfo(serviceHandler.getHttpMethod().name().toLowerCase(), convert(realPath)), serviceHandler);
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

        if (path == null) return null;

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

        @Override
        public String toString() {
            return "ServiceHandlerInfo{" +
                    "method='" + method + '\'' +
                    ", path='" + path + '\'' +
                    '}';
        }
    }

}
