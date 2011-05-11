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

import com.ning.http.client.Realm;
import com.ning.http.client.Realm.RealmBuilder;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.representation.Form;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.sonatype.restsimple.api.DefaultServiceDefinition;
import org.sonatype.restsimple.api.DeleteServiceHandler;
import org.sonatype.restsimple.api.GetServiceHandler;
import org.sonatype.restsimple.api.MediaType;
import org.sonatype.restsimple.api.PostServiceHandler;
import org.sonatype.restsimple.api.PutServiceHandler;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.api.ServiceHandler;
import org.sonatype.restsimple.spi.ServiceHandlerMapper;
import org.sonatype.spice.jersey.client.ahc.AhcHttpClient;
import org.sonatype.spice.jersey.client.ahc.config.DefaultAhcConfig;

import javax.ws.rs.core.UriBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A simple HTTP client which support {@link ServiceDefinition} as a source of information when executing the request.
 * The information contained in {@link ServiceDefinition} will be used when setting the content-type, accept, etc. and
 * will also be used to serialize and de-serialize the request/response. As an example, you can create a simple petstore
 * application by doing
 * <pre>
 * {@code
 *
 *      Web web = new WebAHCClient(serviceDefinition);
        Map<String, String> m = new HashMap<String, String>();
        m.put("Content-Type", acceptHeader);

        Pet pet = web.clientOf(targetUrl + "/addPet/myPet")
                .headers(m)
                .post(new Pet("pouetpouet"), Pet.class);

 * }
 * </pre>
 * The class can also be used without a service definition. All the request information must be "manually" configured.
 * <pre>
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
 * </pre>
 * The client support content negotiation as defined in RFC 2295 via the
 * {@link WebAHCClient#supportedContentType(org.sonatype.restsimple.api.MediaType)}
 *
 * This client build on top of the Sonatype's Jersey AHC Client.
 */
public class WebAHCClient implements WebClient {
    public final boolean compatWithSitebricks;

    private String uri;
    private final ServiceDefinition serviceDefinition;
    private Map<String, String> headers = Collections.emptyMap();
    private Map<String, String> queryString = Collections.emptyMap();
    private Map<String, String> matrixParams = Collections.emptyMap();
    private List<MediaType> supportedContentType = new ArrayList<MediaType>();
    private NegotiationHandler negotiateHandler;

    private Realm realm;

    /**
     * Create a WebAHCClient Client
     */
    public WebAHCClient() {
        this(new DefaultServiceDefinition());
    }

    /**
     * Create a WebAHCClient Client and populate it using the {@link ServiceDefinition}
     *
     * @param serviceDefinition a {@link ServiceDefinition}
     */
    public WebAHCClient(ServiceDefinition serviceDefinition) {
        this(serviceDefinition, new RFC2295NegotiationHandler());
    }

        /**
     * Create a WebAHCClient Client and populate it using the {@link ServiceDefinition}
     *
     * @param serviceDefinition a {@link ServiceDefinition}
     */
    public WebAHCClient(ServiceDefinition serviceDefinition,  boolean compatWithSitebricks) {
        this(serviceDefinition, new RFC2295NegotiationHandler(), compatWithSitebricks);
    }

    /**
     * Create a WebAHCClient Client and populate it using the {@link ServiceDefinition}.
     *
     * @param serviceDefinition a {@link ServiceDefinition}
     * @param negotiateHandler  an implementation of {@link NegotiationHandler}
     */
    public WebAHCClient(ServiceDefinition serviceDefinition, NegotiationHandler negotiateHandler) {
        this(serviceDefinition, negotiateHandler, false);
    }

    /**
     * Create a WebAHCClient Client and populate it using the {@link ServiceDefinition}.
     *
     * @param serviceDefinition a {@link ServiceDefinition}
     * @param negotiateHandler  an implementation of {@link NegotiationHandler}
     */
    public WebAHCClient(ServiceDefinition serviceDefinition, NegotiationHandler negotiateHandler, boolean compatWithSitebricks) {
        this.serviceDefinition = serviceDefinition;
        this.negotiateHandler = negotiateHandler;
        this.compatWithSitebricks = compatWithSitebricks;
    }

    /**
     * Configure the headers of the request.
     *
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
     *
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
     *
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
     *
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
     *
     * @param formParams A Map of forms parameters
     * @param t          A class of type T that will be used when serializing/deserializing the request/response body.
     * @param <T>
     * @return An instance of t
     */
    @Override
    public <T> T post(Map<String, String> formParams, Class<T> t) {
        AhcHttpClient asyncClient = AhcHttpClient.create(createAhcConfig());
        try {
            Form form = new Form();
            for (Map.Entry<String, String> e : formParams.entrySet()) {
                form.add(e.getKey(), e.getValue());
            }
            WebResource r = buildRequest(asyncClient);
            return headers(r, TYPE.POST, true).post(t, form);
        } catch (UniformInterfaceException u) {
            headers.put(negotiateHandler.challengedHeaderName(), negotiate(u));
            return post(formParams, t);
        } finally {
            asyncClient.destroy();
        }
    }

    /**
     * Execute a POST operation
     *
     * @param o              An object that will be serialized as the request body
     * @param responseEntity A class of type T that will be used when serializing the response's body.
     * @param <T>
     * @return An instance of t
     */
    @Override
    public <T> T post(Object o, Class<T> responseEntity) {
        AhcHttpClient asyncClient = AhcHttpClient.create(createAhcConfig());

        o = quoteString(o);
        try {
            WebResource r = buildRequest(asyncClient);
            ClientResponse response = headers(r, TYPE.POST).post(ClientResponse.class, o);
            if (response.getStatus() > 300) {
                throw new WebException(response.getStatus(), response.getClientResponseStatus().getReasonPhrase());
            }
            return checkVoid(response, responseEntity);
        } catch (UniformInterfaceException u) {
            headers.put(negotiateHandler.challengedHeaderName(), negotiate(u));
            return post(o, responseEntity);
        } finally {
            asyncClient.destroy();
        }
    }

    /**
     * Execute a POST operation
     *
     * @param o An object that will be serialized as the request body
     * @return An Object representing the response's body
     */
    @Override
    public Object post(Object o) {
        AhcHttpClient asyncClient = AhcHttpClient.create(createAhcConfig());

        o = quoteString(o);
        try {
            WebResource r = buildRequest(asyncClient);
            return headers(r, TYPE.POST).post(findEntity(r, TYPE.POST), o);
        } catch (UniformInterfaceException u) {
            headers.put(negotiateHandler.challengedHeaderName(), negotiate(u));
            return post(o);
        } finally {
            asyncClient.destroy();
        }
    }

    /**
     * Execute a DELETE operation
     *
     * @param o An object that will be serialized as the request body
     * @return An Object representing the response's body
     */
    @Override
    public Object delete(Object o) {
        AhcHttpClient asyncClient = AhcHttpClient.create(createAhcConfig());

        o = quoteString(o);
        try {
            WebResource r = buildRequest(asyncClient);
            return headers(r, TYPE.DELETE).delete(findEntity(r, TYPE.DELETE), o);
        } catch (UniformInterfaceException u) {
            headers.put(negotiateHandler.challengedHeaderName(), negotiate(u));
            return delete(o);
        } finally {
            asyncClient.destroy();
        }
    }

    /**
     * Execute a DELETE operation
     *
     * @param t A class of type T that will be used when serializing/deserializing the request/response body.
     * @return A T representing the response's body
     */
    @Override
    public <T> T delete(Class<T> t) {
        AhcHttpClient asyncClient = AhcHttpClient.create(createAhcConfig());

        try {
            WebResource r = buildRequest(asyncClient);
            return headers(r, TYPE.DELETE).delete(t);
        } catch (UniformInterfaceException u) {
            headers.put(negotiateHandler.challengedHeaderName(), negotiate(u));
            return delete(t);
        } finally {
            asyncClient.destroy();
        }
    }

    /**
     * Execute a DELETE operation
     *
     * @return An Object representing the response's body
     */
    @Override
    public Object delete() {
        AhcHttpClient asyncClient = AhcHttpClient.create(createAhcConfig());

        try {
            WebResource r = buildRequest(asyncClient);
            return headers(r, TYPE.DELETE).delete(findEntity(r, TYPE.DELETE));
        } catch (UniformInterfaceException u) {
            headers.put(negotiateHandler.challengedHeaderName(), negotiate(u));
            return delete();
        } finally {
            asyncClient.destroy();
        }
    }

    /**
     * Execute a DELETE operation
     *
     * @param o              An object that will be serialized as the request body
     * @param responseEntity A class of type T that will be used when serializing the response's body.
     * @param <T>
     * @return An instance of t
     */
    @Override
    public <T> T delete(Object o, Class<T> responseEntity) {
        AhcHttpClient asyncClient = AhcHttpClient.create(createAhcConfig());

        o = quoteString(o);
        try {
            WebResource r = buildRequest(asyncClient);
            ClientResponse response = headers(r, TYPE.DELETE).delete(ClientResponse.class, o);
            if (response.getStatus() > 300) {
                throw new WebException(response.getStatus(), response.getClientResponseStatus().getReasonPhrase());
            }
            return checkVoid(response, responseEntity);
        } catch (UniformInterfaceException u) {
            headers.put(negotiateHandler.challengedHeaderName(), negotiate(u));
            return delete(o, responseEntity);
        } finally {
            asyncClient.destroy();
        }
    }

    /**
     * Execute a GET operation
     *
     * @param t A class of type T that will be used when serializing/deserializing the request/response body.
     * @return An T representing the response's body
     */
    @Override
    public <T> T get(Class<T> t) {
        AhcHttpClient asyncClient = AhcHttpClient.create(createAhcConfig());

        try {
            WebResource r = buildRequest(asyncClient);
            return headers(r, TYPE.GET).get(t);
        } catch (UniformInterfaceException u) {
            headers.put(negotiateHandler.challengedHeaderName(), negotiate(u));
            return get(t);
        } finally {
            asyncClient.destroy();
        }
    }
        
    /**
     * Execute a GET operation
     *
     * @return An Object representing the response's body
     */
    @Override
    public Object get() {
        AhcHttpClient asyncClient = AhcHttpClient.create(createAhcConfig());

        try {
            WebResource r = buildRequest(asyncClient);
            return headers(r, TYPE.GET).get(findEntity(r, TYPE.GET));
        } catch (UniformInterfaceException u) {
            headers.put(negotiateHandler.challengedHeaderName(), negotiate(u));
            return get();
        } finally {
            asyncClient.destroy();
        }
    }

    /**
     * Execute a PUT operation
     *
     * @param o              An object that will be serialized as the request body
     * @param responseEntity A class of type T that will be used when serializing the response's body.
     * @param <T>
     * @return An instance of t
     */
    @Override
    public <T> T put(Object o, Class<T> responseEntity) {
        AhcHttpClient asyncClient = AhcHttpClient.create(createAhcConfig());

        o = quoteString(o);
        try {
            WebResource r = buildRequest(asyncClient);
            r.entity(o);
            ClientResponse response = headers(r, TYPE.PUT).put(ClientResponse.class, o);
            if (response.getStatus() > 300) {
                throw new WebException(response.getStatus(), response.getClientResponseStatus().getReasonPhrase());
            }
            return checkVoid(response, responseEntity);
        } catch (UniformInterfaceException u) {
            headers.put(negotiateHandler.challengedHeaderName(), negotiate(u));
            return put(o, responseEntity);
        } finally {
            asyncClient.destroy();
        }
    }

    /**
     * Execute a PUT operation
     *
     * @param o An object that will be serialized as the request body
     * @return A T representing the response's body
     */
    @Override
    public Object put(Object o) {
        AhcHttpClient asyncClient = AhcHttpClient.create(createAhcConfig());

        o = quoteString(o);
        try {
            WebResource r = buildRequest(asyncClient);
            return headers(r, TYPE.PUT).put(findEntity(r, TYPE.PUT), o);
        } catch (UniformInterfaceException u) {
            headers.put(negotiateHandler.challengedHeaderName(), negotiate(u));
            return put(o);
        } finally {
            asyncClient.destroy();
        }
    }

    private <T> T checkVoid(ClientResponse response, Class<T> responseEntity) {
        if (Void.class.isAssignableFrom(responseEntity) || responseEntity == void.class) {
            return null;
        } else {
            return response.getEntity(responseEntity);
        }
    }

    protected Object quoteString(Object s) {
        if (compatWithSitebricks && String.class.isAssignableFrom(s.getClass())) {
            return "\"" + s + "\"";
        } else {
            return s;
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

    @Override
    public WebClient auth(final AuthScheme scheme, final String user, final String password) {

        realm = new RealmBuilder().setPrincipal(user).setPrincipal(password).setScheme(mapScheme(scheme)).build();

        return this;
    }

    private DefaultAhcConfig createAhcConfig(){
        DefaultAhcConfig ahcConfig = new DefaultAhcConfig();
                ahcConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        ahcConfig.getClasses().add(JacksonJsonProvider.class);
        ahcConfig.getAsyncHttpClientConfigBuilder().setAllowPoolingConnection(false);

        if (realm != null) {
            ahcConfig.getAsyncHttpClientConfigBuilder().setRealm(realm);
        }

        return ahcConfig;
    }

    private Realm.AuthScheme mapScheme(AuthScheme scheme) {

        switch (scheme) {
            case BASIC:
                return Realm.AuthScheme.BASIC;
            case DIGEST:
                return Realm.AuthScheme.DIGEST;
            case KERBEROS:
                return Realm.AuthScheme.KERBEROS;
            case SPNEGO:
                return Realm.AuthScheme.SPNEGO;
            default:
                throw new IllegalStateException();
        }
    }

    private String negotiate(UniformInterfaceException u) {
        return negotiateHandler.negotiate(supportedContentType, u.getResponse().getHeaders(), u.getResponse().getStatus(), u.getResponse().getClientResponseStatus().getReasonPhrase());
    }

    private WebResource buildRequest(AhcHttpClient asyncClient) {
        UriBuilder u = UriBuilder.fromUri(uri);
        if (matrixParams.size() > 0) {
            for (Map.Entry<String, String> e : matrixParams.entrySet()) {
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
        return String.class;
    }

    private WebResource.Builder headers(WebResource r, TYPE type) {
        return headers(r, type, false);
    }

    @Override
    public String toString() {
        return "WebAHCClient{" +
                "uri='" + uri + '\'' +
                ", serviceDefinition=" + serviceDefinition +
                ", headers=" + headers +
                ", queryString=" + queryString +
                ", matrixParams=" + matrixParams +
                ", supportedContentType=" + supportedContentType +
                '}';
    }

    private WebResource.Builder headers(WebResource r, TYPE type, boolean formEncoded) {
        WebResource.Builder builder = r.getRequestBuilder();
        ServiceHandlerMapper mapper = new ServiceHandlerMapper(serviceDefinition.serviceHandlers());
        boolean contentTypeSet = false;
        boolean acceptTypeSet = false;

        String urlPath = r.getURI().getPath();
        String path = serviceDefinition.path();
        if (!path.equals("") || !path.equals("/")) {
            urlPath = urlPath.substring(urlPath.indexOf(path) + path.length());
        }
        ServiceHandler sh = null;

        for (String p : urlPath.split("/")) {
            sh = mapper.map(type.name().toLowerCase(), p);
            if (sh != null) break;
        }

        if (sh == null) {
            sh = mapper.map(type.name().toLowerCase(), urlPath);
        }

        List<MediaType> list;
        if (sh != null) {
            list = sh.mediaToProduce();
        } else {
            list = new ArrayList<MediaType>();
            list = serviceDefinition.mediaToProduce();
        }

        if (sh != null && sh.consumeMediaType() != null) {
            builder.header("Accept", sh.consumeMediaType().toMediaType());
            acceptTypeSet = true;
        }

        if (list.size() > 0) {
            for (MediaType m : list) {
                if (headers.get("Content-Type") == null && !formEncoded) {
                    builder.header("Content-Type", m.toMediaType());
                    contentTypeSet = true;
                }
            }
        }

        if (headers.size() > 0) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                builder.header(e.getKey(), e.getValue());
                if (e.getKey().equalsIgnoreCase("Content-Type")) {
                    contentTypeSet = true;
                } else if (e.getKey().equalsIgnoreCase("Accept")) {
                    acceptTypeSet = true;
                }
            }
        }

        if (!contentTypeSet && !formEncoded) {
            builder.header("Content-Type", "text/json");
        }

        if (!acceptTypeSet) {
            builder.header("Accept", "*/*");
        }
        return builder;
    }
}
