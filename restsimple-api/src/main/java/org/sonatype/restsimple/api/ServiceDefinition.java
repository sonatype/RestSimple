
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
package org.sonatype.restsimple.api;

import com.google.inject.ImplementedBy;

import java.util.List;

/**
 * A ServiceDefinition represents a REST resource. A ServiceDefinition gets translated by this framework into a resource
 * that can be deployed or integrated into other framework like SiteBricks or Jersey (JAXRS).
 *
 * As an example, the following ServiceDefinition 
 * {@code
 *
        Injector injector = Guice.createInjector(new RestSimpleSitebricksModule(binder()));
        Action action = new PetstoreAction();

        ServiceDefinition serviceDefinition = injector.getInstance(ServiceDefinition.class);
        serviceDefinition
                .withHandler(new GetServiceHandler("/{getPet}", action).consumeWith(JSON, Pet.class).producing(JSON))
                .withHandler(new PostServiceHandler("/{addPet}", action).consumeWith(JSON, Pet.class).producing(JSON))
                .bind();
 * }
 * can easily be translated into a JAXRS resources by using the restsimple-jaxrs extension. A ServiceDefinition can be
 * seen as a DSL for REST application.
 *
 * Request will be delegated to the {@link Action#action}'s method using the information contained within its associated
 * {@link ServiceHandler}. Using the example above, a request to:
 * <p>
 *   PUT /createAddressBook/myBook
 * <p>
 * will be mapped on the server side to the ServiceHandler defined as
 * <p>
 *      new PutServiceHandler("/createAddressBook/myBook", action))
 * <p>
 * which will invoke the {@link Action}
 * <p>
 *     {@link Action#action(ActionContext)}
 * <p>
 *
 */
@ImplementedBy(DefaultServiceDefinition.class)
public interface ServiceDefinition {

    /**
     * Represent an HTTP Method
     */
    public enum METHOD {
        POST, GET, PUT, DELETE, HEAD
    }

    /**
     * Set the url path this {@link ServiceDefinition} is representing.
     * @param path the uri
     * @return the current {@link ServiceDefinition}
     */
    ServiceDefinition withPath(String path);

    /**
     * Add a {@link ServiceHandler} used when mapping request to a {@link Action}. This method can be invoked many times.
     * @param serviceHandler a {@link ServiceHandler}
     * @return the current {@link ServiceDefinition}
     */
    ServiceDefinition withHandler(ServiceHandler serviceHandler);

    /**
     * Add a {@link org.sonatype.restsimple.api.MediaType} this ServiceDefinition. {@link org.sonatype.restsimple.api.MediaType} a
     * re used when writing the response and maps the HTTP response's content-type header.
     * @param mediaType {@link org.sonatype.restsimple.api.MediaType}
     * @return the current {@link ServiceDefinition}
     */
    ServiceDefinition producing(MediaType mediaType);

    /**
     * Add a {@link org.sonatype.restsimple.api.MediaType} this ServiceDefinition. {@link org.sonatype.restsimple.api.MediaType}
     * are used when reading the request and maps the HTTP request's content-type header.
     * @param mediaType {@link org.sonatype.restsimple.api.MediaType}
     * @return the current {@link ServiceDefinition}
     */
    ServiceDefinition consuming(MediaType mediaType);

    /**
     * Return the URL this ServiceDefinition represent.
     * @return the URL this ServiceDefinition represent.
     */
    String path();

    /**
     * Return an unmodifiable {@link List} of {@link ServiceHandler}
     * @return an unmodifiable {@link List} of {@link ServiceHandler}
     */
    List<ServiceHandler> serviceHandlers();

    /**
     * Return an unmodifiable {@link List} of {@link org.sonatype.restsimple.api.MediaType}
     * @return an unmodifiable {@link List} of {@link org.sonatype.restsimple.api.MediaType}
     */
    List<MediaType> mediaToConsume();

    /**
     * Return an unmodifiable {@link List} of {@link org.sonatype.restsimple.api.MediaType}
     * @return an unmodifiable {@link List} of {@link org.sonatype.restsimple.api.MediaType}
     */
    List<MediaType> mediaToProduce();

    /**
     * Add a extension of the generated REST resource.
     * @param clazz an extension class.
     * @return this
     */
    ServiceDefinition extendWith(Class<?> clazz);

    /**
     * Return the list of REST resource extension
     * @return the list of REST resource extension
     */
    List<Class<?>> extensions();

    /**
     * Support HTTP GET using an {@link Action}
     * @param path  a URI
     * @param action an {@link Action} to invoke
     * @return this
     */
    ServiceDefinition handleWithGet(String path, Action action);

    /**
     * Support HTTP POST using an {@link Action}
     * @param path  a URI
     * @param action an {@link Action} to invoke
     * @return this
     */
    ServiceDefinition handleWithPost(String path, Action action);

    /**
     * Support HTTP PUT using an {@link Action}
     * @param path  a URI
     * @param action an {@link Action} to invoke
     * @return this
     */
    ServiceDefinition handleWithPut(String path, Action action);

    /**
     * Support HTTP DELETE using an {@link Action}
     * @param path  a URI
     * @param action an {@link Action} to invoke
     * @return this
     */
    ServiceDefinition handleWithDelete(String path, Action action);


}
