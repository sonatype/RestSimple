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
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.representation.Form;
import org.sonatype.restsimple.api.DefaultServiceDefinition;
import org.sonatype.restsimple.api.DeleteServiceHandler;
import org.sonatype.restsimple.api.GetServiceHandler;
import org.sonatype.restsimple.api.MediaType;
import org.sonatype.restsimple.api.PostServiceHandler;
import org.sonatype.restsimple.api.PutServiceHandler;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.api.ServiceHandler;
import org.sonatype.restsimple.api.WebClient;
import org.sonatype.spice.jersey.client.ahc.AhcHttpClient;
import org.sonatype.spice.jersey.client.ahc.config.DefaultAhcConfig;

import javax.ws.rs.core.UriBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple HTTP client which support {@link ServiceDefinition} as a source of information when executing the request.
 * The information contained in {@link ServiceDefinition} will be used when setting the content-type, accept, etc. and
 * will also be used to serialize and de-serialize the request/response. As an example, you can create a simple petstore
 * application by doing
 * {@code
 *
 *      Web web = new WebAHCClient(serviceDefinition);
        Map<String, String> m = new HashMap<String, String>();
        m.put("Content-Type", acceptHeader);

        Pet pet = web.clientOf(targetUrl + "/addPet/myPet")
                .headers(m)
                .post(new Pet("pouetpouet"), Pet.class);

 * }
 *
 * The class can also be used without a service definition. All the request information must be "manually" configured.
 * {@code
 *
        Web web = new WebAHCClient();
        Map<String, String> m = new HashMap<String, String>();
        m.put("Content-Type", acceptHeader);
        m.put("Accept", acceptHeader);

        Pet pet = web.clientOf(targetUrl + "/addPet/myPet")
                .headers(m)
                .post(new Pet("pouetpouet"), Pet.class);
 *
 * }
 *
 * The client support content negotiation as defined in RFC 2295 via the
 * {@link WebAHCClient#supportedContentType(org.sonatype.restsimple.api.MediaType)}
 *
 * This client build on top of the Sonatype's Jersey AHC Client.
 */
public class WebAHCClient implements WebClient {

    private String uri;

    private final ServiceDefinition serviceDefinition;

    private Client asyncClient;

    private final AsyncHttpClientConfig.Builder configBuilder;

    private final DefaultAhcConfig ahcConfig;

    private Map<String, String> headers = Collections.emptyMap();
    private Map<String, String> queryString;
    private Map<String, String> matrixParams = Collections.emptyMap();
    private List<MediaType> supportedContentType = new ArrayList<MediaType>();
    private NegotiationHandler negotiateHandler;

    /**
     * Create a WebAHCClient Client
     */
    public WebAHCClient() {
        this(new DefaultServiceDefinition());
    }

    /**
     * Create a WebAHCClient Client and populate it using the {@link ServiceDefinition}
     * @param serviceDefinition a {@link ServiceDefinition}
     */
    public WebAHCClient(ServiceDefinition serviceDefinition) {
        this(new DefaultAhcConfig(), serviceDefinition);
    }

    /**
     * Create a WebAHCClient Client and populate it using the {@link ServiceDefinition}. Custom HTTP client configuration
     * can be made using the {@link DefaultAhcConfig}
     * @param ahcConfig An {@link DefaultAhcConfig}
     * @param serviceDefinition a {@link ServiceDefinition}
     */
    public WebAHCClient(DefaultAhcConfig ahcConfig, ServiceDefinition serviceDefinition) {
        this(ahcConfig, serviceDefinition, new RFC2295NegotiationHandler());
    }
    
    /**
     * Create a WebAHCClient Client and populate it using the {@link ServiceDefinition}. Custom HTTP client configuration
     * can be made using the {@link DefaultAhcConfig}
     * @param ahcConfig An {@link DefaultAhcConfig}
     * @param serviceDefinition a {@link ServiceDefinition}
     * @param negotiateHandler an implementation of {@link NegotiationHandler}
     */
    public WebAHCClient(DefaultAhcConfig ahcConfig, ServiceDefinition serviceDefinition, NegotiationHandler negotiateHandler) {
        this.ahcConfig = ahcConfig;

        ahcConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        
        configBuilder = ahcConfig.getAsyncHttpClientConfigBuilder();
        this.serviceDefinition = serviceDefinition;
        this.negotiateHandler = negotiateHandler;
    }

    /**
     * Create a WebAHCClient Client and populate it using the {@link ServiceDefinition}.
     * @param serviceDefinition a {@link ServiceDefinition}
     * @param negotiateHandler an implementation of {@link NegotiationHandler}
     */
    public WebAHCClient(ServiceDefinition serviceDefinition, NegotiationHandler negotiateHandler) {
        this(new DefaultAhcConfig(), serviceDefinition, negotiateHandler);
    }

    /**
     * Create a WebAHCClient Client and populate it using the {@link ServiceDefinition}. Custom HTTP client configuration
     * can be made using the {@link DefaultAhcConfig}
     * @param ahcConfig An {@link DefaultAhcConfig}
     * @param negotiateHandler an implementation of {@link NegotiationHandler}
     */
    public WebAHCClient(DefaultAhcConfig ahcConfig, NegotiationHandler negotiateHandler) {
        this(ahcConfig, new DefaultServiceDefinition(), negotiateHandler);
    }

    /**
     * Configure the headers of the request.
     * @param headers a {@link Map} of request's headers.
     * @return this
     */
    @Override
    public WebClient headers(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Configure the query string of the request.
     * @param queryString a {@link Map} of request's query string.
     * @return this
     */
    @Override
    public WebClient queryString(Map<String, String> queryString) {
        this.queryString = queryString;
        return this;
    }

    /**
     * Configure the matrix parameters of the request.
     * @param matrixParams a {@link Map} of request's matrix parameters 
     * @return this
     */
    @Override
    public WebClient matrixParams(Map<String, String> matrixParams) {
        this.matrixParams = matrixParams;
        return this;
    }

    /**
     * Set the request URI.
     * @param uri the request URI.
     * @return this
     */
    @Override
    public WebClient clientOf(String uri) {
        this.uri = uri;
        return this;
    }

    /**
     * Execute a POST operation
     * @param formParams  A Map of forms parameters
     * @param t A class of type T that will be used when de-serializing the response's body.
     * @param <T>
     * @return An instance of t
     */
    @Override
    public <T> T post(Map<String, String> formParams, Class<T> t) {
        try {
            Form form = new Form();
            for (Map.Entry<String, String> e : formParams.entrySet()) {
                form.add(e.getKey(), e.getValue());
            }
            WebResource r = buildRequest();
            return headers(r, TYPE.POST, true).post(t, form);
        } catch (UniformInterfaceException u) {
            headers.put(negotiateHandler.challengedHeaderName(), negotiate(u));
            return post(formParams, t);
        }
    }

    /**
     * Execute a POST operation
     * @param o An object that will be serialized as the request body
     * @param t A class of type T that will be used when de-serializing the response's body.
     * @param <T>
     * @return An instance of t
     */
    @Override
    public <T> T post(Object o, Class<T> t) {
        try {

            WebResource r = buildRequest();
            return headers(r, TYPE.POST).post(t, o);
        } catch (UniformInterfaceException u) {
            headers.put(negotiateHandler.challengedHeaderName(), negotiate(u));
            return post(o, t);
        }
    }

    /**
     * Execute a POST operation
     * @param o An object that will be serialized as the request body
     * @return An Object representing the response's body
     */
    @Override
    public Object post(Object o) {
        try {
            WebResource r = buildRequest();
            return headers(r, TYPE.POST).post(findEntity(r, TYPE.POST), o);
        } catch (UniformInterfaceException u) {
            headers.put(negotiateHandler.challengedHeaderName(), negotiate(u));
            return post(o);
        }
    }

    /**
     * Execute a DELETE operation
     * @param o An object that will be serialized as the request body
     * @return An Object representing the response's body
     */
    @Override
    public Object delete(Object o) {
        try {
            WebResource r = buildRequest();
            return headers(r, TYPE.DELETE).delete(findEntity(r, TYPE.DELETE), o);
        } catch (UniformInterfaceException u) {
            headers.put(negotiateHandler.challengedHeaderName(), negotiate(u));
            return delete(o);
        }
    }

    /**
     * Execute a DELETE operation
     * @param t A class of type T that will be used when de-serializing the response's body.
     * @return A T representing the response's body
     */
    @Override
    public <T> T delete(Class<T> t) {
        try {
            WebResource r = buildRequest();
            return headers(r, TYPE.DELETE).delete(t);
        } catch (UniformInterfaceException u) {
            headers.put(negotiateHandler.challengedHeaderName(), negotiate(u));
            return delete(t);
        }
    }

    /**
     * Execute a DELETE operation
     * @return An Object representing the response's body
     */
    @Override
    public Object delete() {
        try {

            WebResource r = buildRequest();
            return headers(r, TYPE.DELETE).delete(findEntity(r, TYPE.DELETE));
        } catch (UniformInterfaceException u) {
            headers.put(negotiateHandler.challengedHeaderName(), negotiate(u));
            return delete();
        }
    }

    /**
     * Execute a DELETE operation
     * @param o An object that will be serialized as the request body
     * @param t A class of type T that will be used when de-serializing the response's body.
     * @param <T>
     * @return An instance of t
     */
    @Override
    public <T> T delete(Object o, Class<T> t) {
        try {
            WebResource r = buildRequest();
            return headers(r, TYPE.DELETE).delete(t, o);
        } catch (UniformInterfaceException u) {
            headers.put(negotiateHandler.challengedHeaderName(), negotiate(u));
            return delete(o, t);
        }
    }

    /**
     * Execute a GET operation
     * @param t A class of type T that will be used when de-serializing the response's body.
     * @return An T representing the response's body
     */
    @Override
    public <T> T get(Class<T> t) {
        try {

            WebResource r = buildRequest();
            return headers(r, TYPE.GET).get(t);
        } catch (UniformInterfaceException u) {
            headers.put(negotiateHandler.challengedHeaderName(), negotiate(u));
            return get(t);
        }
    }

    /**
     * Execute a GET operation
     * @return An Object representing the response's body
     */
    @Override
    public Object get() {
        try {
            WebResource r = buildRequest();
            return headers(r, TYPE.GET).get(findEntity(r, TYPE.GET));
        } catch (UniformInterfaceException u) {
            headers.put(negotiateHandler.challengedHeaderName(), negotiate(u));
            return get();
        }
    }

    /**
     * Execute a PUT operation
     * @param o An object that will be serialized as the request body
     * @param t A class of type T that will be used when de-serializing the response's body.
     * @param <T>
     * @return An instance of t
     */
    @Override
    public <T> T put(Object o, Class<T> t) {
        try {

            WebResource r = buildRequest();
            return headers(r, TYPE.PUT).put(t, o);
        } catch (UniformInterfaceException u) {
            headers.put(negotiateHandler.challengedHeaderName(), negotiate(u));
            return put(o, t);
        }
    }

    /**
     * Execute a PUT operation
     * @param o An object that will be serialized as the request body
     * @return A T representing the response's body
     */
    @Override
    public Object put(Object o) {
        try {

            WebResource r = buildRequest();
            return headers(r, TYPE.PUT).put(findEntity(r, TYPE.PUT), o);
        } catch (UniformInterfaceException u) {
            headers.put(negotiateHandler.challengedHeaderName(), negotiate(u));
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
    @Override
    public WebClient supportedContentType(MediaType mediaType) {
        supportedContentType.add(mediaType);
        return this;
    }

    private String negotiate(UniformInterfaceException u) {
        return negotiateHandler.negotiate(supportedContentType, u.getResponse().getHeaders(), u.getResponse().getStatus(), u.getResponse().getClientResponseStatus().getReasonPhrase());
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
            } 
        }
        return null;
    }

    private WebResource.Builder headers(WebResource r, TYPE type) {
        return headers(r, type, false);
    }

    @Override
    public String toString() {
        return "WebAHCClient{" +
                "uri='" + uri + '\'' +
                ", serviceDefinition=" + serviceDefinition +
                ", asyncClient=" + asyncClient +
                ", configBuilder=" + configBuilder +
                ", ahcConfig=" + ahcConfig +
                ", headers=" + headers +
                ", queryString=" + queryString +
                ", matrixParams=" + matrixParams +
                ", supportedContentType=" + supportedContentType +
                '}';
    }

    private WebResource.Builder headers(WebResource r, TYPE type, boolean formEncoded) {
        WebResource.Builder builder = r.getRequestBuilder();
        for (ServiceHandler s : serviceDefinition.serviceHandlers()) {

            // TODO: not optimal
            boolean mapUri = r.getURI().getPath().contains(s.path());

            List<MediaType> list;
            if (mapUri && type == TYPE.GET && GetServiceHandler.class.isAssignableFrom(s.getClass())) {
                list = s.mediaToProduce();
            } else if (mapUri &&  type == TYPE.POST && PostServiceHandler.class.isAssignableFrom(s.getClass())) {
                list = s.mediaToProduce();
            } else if (mapUri &&  type == TYPE.PUT && PutServiceHandler.class.isAssignableFrom(s.getClass())) {
                list = s.mediaToProduce();
            } else if (mapUri && type == TYPE.DELETE && DeleteServiceHandler.class.isAssignableFrom(s.getClass())) {
                list = s.mediaToProduce();
            } else {
                list = new ArrayList<MediaType>();
                if (mapUri && serviceDefinition != null) {
                    list = serviceDefinition.mediaToProduce();
                }
            }
            
            if (mapUri && s.consumeMediaType() !=  null ) {
                builder.header("Accept", s.consumeMediaType().toMediaType());
            }

            if (list.size() > 0) {
                for (MediaType m : list) {
                    if (headers.get("Content-Type") == null && !formEncoded) {
                        builder.header("Content-Type", m.toMediaType());
                    }
                }
                break;
            }
        }

        if (headers.size() > 0) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                builder.header(e.getKey(), e.getValue());
            }
        }
        return builder;
    }

}
