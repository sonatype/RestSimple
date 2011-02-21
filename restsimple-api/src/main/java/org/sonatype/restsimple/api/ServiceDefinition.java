
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

import org.sonatype.restsimple.spi.ServiceDefinitionGenerator;

import java.util.List;

/**
 * A ServiceDefinition represents a  REST resource. A ServiceDefinition is translated by this framework into a resource
 * that can be deployed or integrated into other framework like SiteBricks or Jersey (JAXRS).
 *
 * As an example, the following ServiceDefinition 
 * {@code
 *
 *      ServiceEntity serviceEntity = new AddressBookServiceEntity();
        bind(Action.class).toInstance(serviceEntity);

        ServiceDefinition serviceDefinition = injector.getInstance(ServiceDefinition.class);
        serviceDefinition.withPath("/{method}/{id}")
                .producing(MediaType.JSON)
                .producing(MediaType.XML)
                .consuming(MediaType.JSON)
                .consuming(MediaType.XML)
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.PUT, "id", "createAddressBook"))
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.GET, "id", "getAddressBook"))
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.POST, "id", "updateAddressBook"))
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.DELETE, "id", "deleteAddressBook"))
                .usingEntity(serviceEntity)
                .bind();
 * }
 * can easily be translated into a JAXRS resources by using the restsimple-jaxrs extension.
 *
 * Request will be delegated to the {@link Action}'s method using the information contained within passed
 * {@link ServiceHandler}. Using the example above, a request to:
 * <p>
 *   PUT /createAddressBook/myBook
 * <p>
 * will be mapped on the server side to the ServiceHandler defined as
 * <p>
 * new ServiceHandler(ServiceDefinition.HttpMethod.PUT, "id", "createAddressBook")
 * <p>
 * which will invoke the {@link Action}
 * <p>
 *     serviceEntity.createAddressBook(myBook)  
 * <p>
 * Note. A {@link ServiceDefinition} gets generated only when {@link ServiceDefinition#bind} gets invoked. You can
 * reconfigure the service at any moment and regenerate it's associated resource at any moment. 
 */
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
     * Add a {@link org.sonatype.restsimple.api.MediaType} this ServiceDefinition. {@link org.sonatype.restsimple.api.MediaType} are used when writing the response and maps the HTTP response's content-type header.
     * @param mediaType {@link org.sonatype.restsimple.api.MediaType}
     * @return the current {@link ServiceDefinition}
     */
    ServiceDefinition producing(MediaType mediaType);

    /**
     * Add a {@link org.sonatype.restsimple.api.MediaType} this ServiceDefinition. {@link org.sonatype.restsimple.api.MediaType} are used when reading the request and maps the HTTP request's content-type header.
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
     * Bind the {@link ServiceDefinition}. This operation may generate a new class based on the information this class is
     * holding. The bind operation can be invoked many time and will generate a server side component on every invocation.
     */
    void bind();

    /**
     * Set the {@link org.sonatype.restsimple.spi.ServiceDefinitionGenerator} used to generate the REST resource.
     * @param generator the {@link org.sonatype.restsimple.spi.ServiceDefinitionGenerator}
     * @return the current {@link ServiceDefinition}
     */
    ServiceDefinition generateWith(ServiceDefinitionGenerator generator);
}
