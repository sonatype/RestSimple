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
package org.sonatype.restsimple;

import com.ning.http.client.AsyncHttpClientConfig;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.sonatype.restsimple.api.DefaultServiceDefinition;
import org.sonatype.restsimple.api.DeleteServiceHandler;
import org.sonatype.restsimple.api.GetServiceHandler;
import org.sonatype.restsimple.api.MediaType;
import org.sonatype.restsimple.api.PostServiceHandler;
import org.sonatype.restsimple.api.PutServiceHandler;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.api.ServiceHandler;
import org.sonatype.spice.jersey.client.ahc.AhcHttpClient;
import org.sonatype.spice.jersey.client.ahc.config.DefaultAhcConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Web {

    private static enum TYPE {
        POST, PUT, DELETE, GET
    }

    private String uri;

    private final ServiceDefinition serviceDefinition;

    private Client asyncClient;

    private final AsyncHttpClientConfig.Builder configBuilder;

    private final DefaultAhcConfig ahcConfig;

    private Map<String, String> headers;
    private Map<String, String> queryString;

    public Web() {
        ahcConfig = new DefaultAhcConfig();
        configBuilder = ahcConfig.getAsyncHttpClientConfigBuilder();
        this.serviceDefinition = new DefaultServiceDefinition();
    }

    public Web(ServiceDefinition serviceDefinition) {
        ahcConfig = new DefaultAhcConfig();
        configBuilder = ahcConfig.getAsyncHttpClientConfigBuilder();
        this.serviceDefinition = serviceDefinition;
    }

    public Web headers(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public Web queryString(Map<String, String> queryString) {
        this.queryString = queryString;
        return this;
    }

    public Web clientOf(String uri) {
        this.uri = uri;
        return this;
    }

    public <T> T post(Map<String, String> formParams, Class<T> t) {
        WebResource r = buildRequest();
        return headers(r, TYPE.POST).post(t, formParams);
    }

    public <T> T post(Object o, Class<T> t) {
        WebResource r = buildRequest();
        return headers(r, TYPE.POST).post(t, o);
    }

    public Object post(Object o) {
        WebResource r = buildRequest();
        return headers(r, TYPE.POST).post(findEntity(r, TYPE.POST), o);
    }

    public <T> T delete(Class<T> t) {
        WebResource r = buildRequest();
        return headers(r, TYPE.DELETE).delete(t);
    }

    public Object delete() {
        WebResource r = buildRequest();
        return headers(r, TYPE.DELETE).delete(findEntity(r, TYPE.DELETE));
    }

    public <T> T delete(Object o, Class<T> t) {
        WebResource r = buildRequest();
        return headers(r, TYPE.DELETE).delete(t, o);
    }

    public <T> T get(Class<T> t) {
        WebResource r = buildRequest();
        return headers(r, TYPE.GET).get(t);
    }

    public Object get() {
        WebResource r = buildRequest();
        return headers(r, TYPE.GET).get(findEntity(r, TYPE.GET));
    }

    public <T> T put(Object o, Class<T> t) {
        WebResource r = buildRequest();
        return headers(r, TYPE.PUT).put(t, o);
    }

    public Object put(Object o) {
        WebResource r = buildRequest();
        return headers(r, TYPE.PUT).put(findEntity(r, TYPE.PUT), o);
    }

    private WebResource buildRequest() {
        asyncClient = AhcHttpClient.create(ahcConfig);
        WebResource r = asyncClient.resource(uri);
        if (queryString != null && queryString.size() > 0) {
            for (Map.Entry<String, String> e : queryString.entrySet()) {
                r = r.queryParam(e.getKey(), e.getValue());
            }
        }
        return r;
    }

    private Class<?> findEntity(WebResource r, TYPE type) {

        Class<?> clazz;
        for (ServiceHandler s : serviceDefinition.serviceHandlers()) {
            clazz = s.consumeClass();

            // The latest entity as the one associated with the proper service handler.
            if (type == TYPE.GET && GetServiceHandler.class.isAssignableFrom(s.getClass())) {
                return clazz;
            } else if (type == TYPE.POST && PostServiceHandler.class.isAssignableFrom(s.getClass())) {
                return clazz;
            } else if (type == TYPE.PUT && PutServiceHandler.class.isAssignableFrom(s.getClass())) {
                return clazz;
            } else if (type == TYPE.DELETE && DeleteServiceHandler.class.isAssignableFrom(s.getClass())) {
                return clazz;
            } else {
                // IllegalStateException ?
            }
        }
        return null;        
    }

    private WebResource.Builder headers(WebResource r, TYPE type) {
        WebResource.Builder builder = r.getRequestBuilder();
        boolean acceptAdded = false;
        for (ServiceHandler s : serviceDefinition.serviceHandlers()) {

            List<MediaType> list;
            if (type == TYPE.GET && GetServiceHandler.class.isAssignableFrom(s.getClass())) {
                list = s.mediaToProduce();
            } else if (type == TYPE.POST && PostServiceHandler.class.isAssignableFrom(s.getClass())) {
                list = s.mediaToProduce();
            } else if (type == TYPE.PUT && PutServiceHandler.class.isAssignableFrom(s.getClass())) {
                list = s.mediaToProduce();
            } else if (type == TYPE.DELETE && DeleteServiceHandler.class.isAssignableFrom(s.getClass())) {
                list = s.mediaToProduce();
            } else {
                list = new ArrayList<MediaType>();
            }

            if (list.size() > 0) {
                for (MediaType m: list) {
                    builder.header("Accept", m.toMediaType());
                    builder.header("Content-Type", m.toMediaType());
                }
                acceptAdded = true;
                break;                
            }
        }

        if (!acceptAdded) {
            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, String> e : headers.entrySet()) {
                    builder.header(e.getKey(), e.getValue());
                }
            }
        }

        return builder;
    }

}
