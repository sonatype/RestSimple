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

import com.google.inject.ImplementedBy;
import org.sonatype.restsimple.api.MediaType;

import java.util.Map;

/**
 * A Web WebClient for RestSimple. 
 */
@ImplementedBy(WebAHCClient.class)
public interface WebClient {

    public static enum TYPE {
        POST, PUT, DELETE, GET
    }

    public enum AuthScheme {
        BASIC, DIGEST, KERBEROS, SPNEGO
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
}
