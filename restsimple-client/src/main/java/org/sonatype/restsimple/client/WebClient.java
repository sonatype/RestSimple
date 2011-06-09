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

import com.google.inject.ImplementedBy;
import org.sonatype.restsimple.api.MediaType;

import java.util.Map;

/**
 * A WebClient for RestSimple.
 */
@ImplementedBy(WebAHCClient.class)
public interface WebClient {

    public static enum TYPE {
        POST, PUT, DELETE, GET
    }

    public enum AuthScheme {
        BASIC, DIGEST, KERBEROS, SPNEGO
    }

    public enum ProxyScheme {
        HTTP, HTTPS, NTLM, KERBEROS, SPNEGO
    }

    /**
     * Configure the headers of the request.
     *
     * @param headers a {@link Map} of request's headers.
     * @return this
     */
    WebClient headers(Map<String, String> headers);

    /**
     * Configure the query string of the request.
     *
     * @param queryString a {@link Map} of request's query string.
     * @return this
     */
    WebClient queryString(Map<String, String> queryString);

    /**
     * Configure the matrix parameters of the request.
     *
     * @param matrixParams a {@link Map} of request's matrix parameters
     * @return this
     */
    WebClient matrixParams(Map<String, String> matrixParams);

    /**
     * Set the request URI.
     *
     * @param uri the request URI.
     * @return this
     */
    WebClient clientOf(String uri);

    /**
     * Execute a POST operation
     *
     * @param formParams A Map of forms parameters
     * @param t          A class of type T that will be used when serializing/deserializing the request/response body.
     * @param <T>
     * @return An instance of t
     */
    <T> T post(Map<String, String> formParams, Class<T> t);

    /**
     * Execute a POST operation
     *
     * @param o   An object that will be serialized as the request body
     * @param t   A class of type T that will be used when serializing/deserializing the request/response body.
     * @param <T>
     * @return An instance of t
     */
    <T> T post(Object o, Class<T> t);

    /**
     * Execute a POST operation
     *
     * @param o An object that will be serialized as the request body
     * @return An Object representing the response's body
     */
    Object post(Object o);

    /**
     * Execute a DELETE operation
     *
     * @param o An object that will be serialized as the request body
     * @return An Object representing the response's body
     */
    Object delete(Object o);

    /**
     * Execute a DELETE operation
     *
     * @param t A class of type T that will be used when serializing/deserializing the request/response body.
     * @return A T representing the response's body
     */
    <T> T delete(Class<T> t);

    /**
     * Execute a DELETE operation
     *
     * @return An Object representing the response's body
     */
    Object delete();

    /**
     * Execute a DELETE operation
     *
     * @param o   An object that will be serialized as the request body
     * @param t   A class of type T that will be used when serializing/deserializing the request/response body.
     * @param <T>
     * @return An instance of t
     */
    <T> T delete(Object o, Class<T> t);

    /**
     * Execute a GET operation
     *
     * @param t A class of type T that will be used when serializing/deserializing the request/response body.
     * @return An T representing the response's body
     */
    <T> T get(Class<T> t);

    /**
     * Execute a GET operation
     *
     * @return An Object representing the response's body
     */
    Object get();

    /**
     * Execute a PUT operation
     *
     * @param o   An object that will be serialized as the request body
     * @param t   A class of type T that will be used when serializing/deserializing the request/response body.
     * @param <T>
     * @return An instance of t
     */
    <T> T put(Object o, Class<T> t);

    /**
     * Execute a PUT operation
     *
     * @param o An object that will be serialized as the request body
     * @return A T representing the response's body
     */
    Object put(Object o);

    /**
     * Add a {@link org.sonatype.restsimple.api.MediaType} to the list of supported content-type. The list of supported content-type
     * is used when the server returns a http statis code of 206.
     *
     * @param mediaType
     * @return this
     */
    WebClient supportedContentType(MediaType mediaType);

    /**
     * Set the authentication user and password as well as the scheme to use.
     *
     * @param scheme   the AuthScheme
     * @param user     the user
     * @param password the passwrod
     * @return this
     */
    WebClient auth(AuthScheme scheme, String user, String password);

    /**
     * Set the Proxy information
     *
     * @param scheme The proxy protocol scheme
     * @param host the proxy uri
     * @param port the proxy port
     * @param user the proxy principal
     * @param password the proxy password
     * @return
     */
    WebClient proxyWith(ProxyScheme scheme, String host, int port, String user, String password);
}
