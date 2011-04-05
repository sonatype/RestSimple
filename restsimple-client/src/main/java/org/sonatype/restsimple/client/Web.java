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

import com.ning.http.client.AsyncHttpClientConfig;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
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

import javax.ws.rs.core.UriBuilder;
import java.util.ArrayList;
import java.util.Collections;
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

    private Map<String, String> headers = Collections.emptyMap();
    private Map<String, String> queryString;
    private Map<String, String> matrixParams = Collections.emptyMap();
    private List<MediaType> supportedContentType = new ArrayList<MediaType>();


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

    public Web matrixParams(Map<String, String> matrixParams) {
        this.matrixParams = matrixParams;
        return this;
    }

    public Web clientOf(String uri) {
        this.uri = uri;
        return this;
    }

    public <T> T post(Map<String, String> formParams, Class<T> t) {
        try {
            Form form = new Form();
            for (Map.Entry<String, String> e : formParams.entrySet()) {
                form.add(e.getKey(), e.getValue());
            }
            WebResource r = buildRequest();
            return headers(r, TYPE.POST, true).post(t, form);
        } catch (UniformInterfaceException u) {
            headers.put("Accept", negotiate(u));
            return post(formParams, t);
        }
    }

    public <T> T post(Object o, Class<T> t) {
        try {

            WebResource r = buildRequest();
            return headers(r, TYPE.POST).post(t, o);
        } catch (UniformInterfaceException u) {
            headers.put("Accept", negotiate(u));
            return post(o, t);
        }
    }

    public Object post(Object o) {
        try {
            WebResource r = buildRequest();
            return headers(r, TYPE.POST).post(findEntity(r, TYPE.POST), o);
        } catch (UniformInterfaceException u) {
            headers.put("Accept", negotiate(u));
            return post(o);
        }
    }

    public Object delete(Object o) {
        try {
            WebResource r = buildRequest();
            return headers(r, TYPE.DELETE).post(findEntity(r, TYPE.DELETE), o);
        } catch (UniformInterfaceException u) {
            headers.put("Accept", negotiate(u));
            return delete(o);
        }
    }

    public <T> T delete(Class<T> t) {
        try {

            WebResource r = buildRequest();
            return headers(r, TYPE.DELETE).delete(t);
        } catch (UniformInterfaceException u) {
            headers.put("Accept", negotiate(u));
            return delete(t);
        }
    }

    public Object delete() {
        try {

            WebResource r = buildRequest();
            return headers(r, TYPE.DELETE).delete(findEntity(r, TYPE.DELETE));
        } catch (UniformInterfaceException u) {
            headers.put("Accept", negotiate(u));
            return delete();
        }
    }

    public <T> T delete(Object o, Class<T> t) {
        try {

            WebResource r = buildRequest();
            return headers(r, TYPE.DELETE).delete(t, o);
        } catch (UniformInterfaceException u) {
            headers.put("Accept", negotiate(u));
            return delete(o, t);
        }
    }

    public <T> T get(Class<T> t) {
        try {

            WebResource r = buildRequest();
            return headers(r, TYPE.GET).get(t);
        } catch (UniformInterfaceException u) {
            headers.put("Accept", negotiate(u));
            return get(t);
        }
    }

    public Object get() {
        try {
            WebResource r = buildRequest();
            return headers(r, TYPE.GET).get(findEntity(r, TYPE.GET));
        } catch (UniformInterfaceException u) {
            headers.put("Accept", negotiate(u));
            return get();
        }
    }

    public <T> T put(Object o, Class<T> t) {
        try {

            WebResource r = buildRequest();
            return headers(r, TYPE.PUT).put(t, o);
        } catch (UniformInterfaceException u) {
            headers.put("Accept", negotiate(u));
            return put(o, t);
        }
    }

    public Object put(Object o) {
        try {

            WebResource r = buildRequest();
            return headers(r, TYPE.PUT).put(findEntity(r, TYPE.PUT), o);
        } catch (UniformInterfaceException u) {
            headers.put("Accept", negotiate(u));
            return put(o);
        }
    }

    /**
     * Add a {@link MediaType} to the list of supported content-type. The list of supported content-type
     * is used when the server returns a http statis code of 206.
     *
     * @param mediaType
     * @return this
     */
    public Web supportedContentType(MediaType mediaType) {
        supportedContentType.add(mediaType);
        return this;
    }

    private String negotiate(UniformInterfaceException u) {
        if (u.getResponse().getStatus() == 206 && supportedContentType.size() > 0) {
            String[] serverChallenge = u.getResponse().getHeaders().get("Accept-Content-Type").get(0).split(",");
            for (String challenge : serverChallenge) {
                for (MediaType m: supportedContentType) {
                    if (challenge.equalsIgnoreCase(m.toMediaType())) {
                        return challenge;
                    }
                }
            }
        }
        throw new WebException(u.getResponse().getStatus(), u.getResponse().getClientResponseStatus().getReasonPhrase());
    }

    private WebResource buildRequest() {
        asyncClient = AhcHttpClient.create(ahcConfig);
        UriBuilder u = UriBuilder.fromUri(uri);
        if (matrixParams.size() > 0) {
            for (Map.Entry<String,String> e: matrixParams.entrySet()){
                u.matrixParam(e.getKey(), e.getValue());
            }
        }         
        WebResource r = asyncClient.resource(u.build());

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
        return headers(r, type, false);
    }

    private WebResource.Builder headers(WebResource r, TYPE type, boolean formEncoded) {
        WebResource.Builder builder = r.getRequestBuilder();
        boolean acceptAdded = false;
        boolean contentTypeAdded = formEncoded;
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
                if (serviceDefinition != null) {
                    list = serviceDefinition.mediaToProduce();
                }
            }

            if (list.size() > 0) {
                for (MediaType m : list) {
                    if (headers.get("Accept") == null) {
                        builder.header("Accept", m.toMediaType());
                        acceptAdded = true;
                    }

                    if (headers.get("Content-Type") == null && !formEncoded) {
                        builder.header("Content-Type", m.toMediaType());
                        contentTypeAdded = true;
                    }
                }
                acceptAdded = true;
                break;
            }
        }

        if (headers.size() > 0) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                // Do not override
                String headerName = e.getKey();
                if (headerName.equalsIgnoreCase("Accept") && acceptAdded) {
                    continue;
                } else if (headerName.equalsIgnoreCase("Content-Type") && contentTypeAdded) {
                    continue;
                } else {
                    builder.header(e.getKey(), e.getValue());
                }
            }
        }
        return builder;
    }

}
