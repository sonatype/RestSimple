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

import com.ning.http.client.ProxyServer;
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
    private ProxyServer proxyServer;

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
     * {@inheritDoc}
     */
    @Override
    public WebClient headers(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebClient queryString(Map<String, String> queryString) {
        this.queryString = queryString;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebClient matrixParams(Map<String, String> matrixParams) {
        this.matrixParams = matrixParams;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebClient clientOf(String uri) {
        this.uri = uri;
        return this;
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public <T> T post(Object o, Class<T> responseEntity) {
        AhcHttpClient asyncClient = AhcHttpClient.create(createAhcConfig());

        o = quoteString(o);
        try {
            WebResource r = buildRequest(asyncClient);
            ClientResponse response = headers(r, TYPE.POST).post(ClientResponse.class, o);
            checkStatus(response);
            return checkVoid(response, responseEntity);
        } catch (UniformInterfaceException u) {
            headers.put(negotiateHandler.challengedHeaderName(), negotiate(u));
            return post(o, responseEntity);
        } finally {
            asyncClient.destroy();
        }
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public <T> T delete(Object o, Class<T> responseEntity) {
        AhcHttpClient asyncClient = AhcHttpClient.create(createAhcConfig());

        o = quoteString(o);
        try {
            WebResource r = buildRequest(asyncClient);
            ClientResponse response = headers(r, TYPE.DELETE).delete(ClientResponse.class, o);
            checkStatus(response);
            return checkVoid(response, responseEntity);
        } catch (UniformInterfaceException u) {
            headers.put(negotiateHandler.challengedHeaderName(), negotiate(u));
            return delete(o, responseEntity);
        } finally {
            asyncClient.destroy();
        }
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public <T> T put(Object o, Class<T> responseEntity) {
        AhcHttpClient asyncClient = AhcHttpClient.create(createAhcConfig());

        o = quoteString(o);
        try {
            WebResource r = buildRequest(asyncClient);
            r.entity(o);
            ClientResponse response = headers(r, TYPE.PUT).put(ClientResponse.class, o);
            checkStatus(response);
            return checkVoid(response, responseEntity);
        } catch (UniformInterfaceException u) {
            headers.put(negotiateHandler.challengedHeaderName(), negotiate(u));
            return put(o, responseEntity);
        } finally {
            asyncClient.destroy();
        }
    }

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
     */
    @Override
    public WebClient supportedContentType(MediaType mediaType) {
        supportedContentType.add(mediaType);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebClient auth(final AuthScheme scheme, final String user, final String password) {
        realm = new RealmBuilder().setPrincipal(user).setPassword(password).setScheme(mapScheme(scheme)).build();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebClient proxyWith(final ProxyScheme scheme, String host, int port, String user, String password) {
        proxyServer = new ProxyServer(mapProxyScheme(scheme), host, port, user, password);
        return null;

    }

    private DefaultAhcConfig createAhcConfig(){
        DefaultAhcConfig ahcConfig = new DefaultAhcConfig();
                ahcConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        ahcConfig.getClasses().add(JacksonJsonProvider.class);
        ahcConfig.getAsyncHttpClientConfigBuilder().setAllowPoolingConnection(false);

        if (realm != null) {
            ahcConfig.getAsyncHttpClientConfigBuilder().setRealm(realm);
        }

        if (proxyServer != null) {
            ahcConfig.getAsyncHttpClientConfigBuilder().setProxyServer(proxyServer);
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

    private ProxyServer.Protocol mapProxyScheme(ProxyScheme scheme) {

        switch (scheme) {
            case HTTP:
                return ProxyServer.Protocol.HTTP;
            case HTTPS:
                return ProxyServer.Protocol.HTTPS;
            case NTLM:
                return ProxyServer.Protocol.NTLM;
            case KERBEROS:
                return ProxyServer.Protocol.KERBEROS;
            case SPNEGO:
                return ProxyServer.Protocol.SPNEGO;
            default:
                throw new IllegalStateException();
        }
    }

    private String negotiate(UniformInterfaceException u) {
        return negotiateHandler.negotiate(supportedContentType,
                u.getResponse().getHeaders(),
                u.getResponse().getStatus(),
                u.getResponse().getClientResponseStatus().getReasonPhrase());
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

    private void checkStatus(ClientResponse response) {
        if (response.getStatus() > 299) {
            String reasonPhrase = "";
            if (response.getClientResponseStatus() != null) {
                reasonPhrase = response.getClientResponseStatus().getReasonPhrase();
            }
            throw new WebException(response.getStatus(), reasonPhrase);
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
}
