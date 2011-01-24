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
package org.sonatype.rest.client;

import com.ning.http.client.Response;

import java.util.Map;

/**
 * Implementation of this class will be generated by an instance of {@link ServiceDefinitionProxy} based on the information
 * translated from a {@link org.sonatype.rest.api.ServiceDefinition}'s list of {@link org.sonatype.rest.api.ServiceHandler}.
 *
 * As an example, the following {@link org.sonatype.rest.api.ServiceDefinition} will generate an implementation of this
 * class:
 *
 * {@code
 *
        serviceDefinition = new DefaultServiceDefinition();

        serviceDefinition .withPath("http://....")
                .producing(ServiceDefinition.MediaType.JSON)
                .producing(ServiceDefinition.MediaType.XML)
                .consuming(ServiceDefinition.MediaType.JSON)
                .consuming(ServiceDefinition.MediaType.XML)
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.PUT, "id", "createAddressBook"))
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.GET, "id", "getAddressBook"))
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.POST, "id", "updateAddressBook"))
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.DELETE, "id", "deleteAddressBook"))
                .usingEntity(new AddressBookServiceEntity());
 *
 *  will translate to
 *
 *       ServiceDefinitionClient stub = ServiceDefinitionProxy.getProxy(serviceDefinition);
 *       stub.doGet("myBook");
 *
 *  will remotely invokes the AddressBookServiceEntity#getAddressBook("myBook");
 * }
 * }
 */
public abstract class ServiceDefinitionClient {

    /**
     * Execute a remote GET operation. The GET operation is generated using a {@link org.sonatype.rest.api.ServiceHandler}
     * and the default URI is from {@link org.sonatype.rest.api.ServiceHandler#getMethod()}. The paths values
     * is defined from {@link org.sonatype.rest.api.ServiceHandler#getPath()} instance.
     * 
     * @param paths A list of URI to execute a remote resource
     * @return a {@link Response} 
     */
    abstract public Response doGet(String... paths);

    /**
     * Execute a remote HEAD operation. The HEAD operation is generated using a {@link org.sonatype.rest.api.ServiceHandler}
     * and the default URI is from {@link org.sonatype.rest.api.ServiceHandler#getMethod()}. The paths values
     * is defined from {@link org.sonatype.rest.api.ServiceHandler#getPath()} instance.
     *
     * @param paths A list of URI to execute a remote resource
     * @return a {@link Response}
     */
    abstract public  Response doHead(String... paths);

    /**
     * Execute a remote POST operation. The POST operation is generated using a {@link org.sonatype.rest.api.ServiceHandler}
     * and the default URI is from {@link org.sonatype.rest.api.ServiceHandler#getMethod()}. The paths values
     * is defined from {@link org.sonatype.rest.api.ServiceHandler#getPath()} instance.
     *
     * @param paths A list of URI to execute a remote resource
     * @return a {@link Response}
     */
    abstract public Response doPut(String... paths);

    /**
     * Execute a remote POST operation. The POST operation is generated using a {@link org.sonatype.rest.api.ServiceHandler}
     * and the default URI is from {@link org.sonatype.rest.api.ServiceHandler#getMethod()}. The paths values
     * is defined from {@link org.sonatype.rest.api.ServiceHandler#getPath()} instance.
     *
     * @param map a {@link Map} where key and value are String, which gets translated to a list of POST parameters. 
     * @param paths A list of URI to execute a remote resource
     * @return a {@link Response}
     */
    abstract public Response doPost(Map<String, String> map, String... paths);

    /**
     * Execute a remote DELETE operation. The DELETE operation is generated using a {@link org.sonatype.rest.api.ServiceHandler}
     * and the default URI is from {@link org.sonatype.rest.api.ServiceHandler#getMethod()}. The paths values
     * is defined from {@link org.sonatype.rest.api.ServiceHandler#getPath()} instance.
     *
     * @param paths A list of URI to execute a remote resource
     * @return a {@link Response}
     */
    abstract public Response doDelete(String... paths);

}
