package org.sonatype.rest.api;

import java.util.List;

/**
 * A ServiceDefinition represents a  REST resource. A ServiceDefinition is translated by this framework into a resource
 * that can be deployed or integrated into other framework like SiteBricks or Jersey (JAXRS).
 *
 * As an example, the following ServiceDefinition 
 * {@code
 *
 *      ServiceEntity serviceEntity = new AddressBookServiceEntity();
        bind(ServiceEntity.class).toInstance(serviceEntity);

        ServiceDefinition serviceDefinition = injector.getInstance(ServiceDefinition.class);
        serviceDefinition.withPath("/{method}/{id}")
                .producing(ServiceDefinition.Media.JSON)
                .producing(ServiceDefinition.Media.XML)
                .consuming(ServiceDefinition.Media.JSON)
                .consuming(ServiceDefinition.Media.XML)
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.PUT, "id", "createAddressBook"))
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.GET, "id", "getAddressBook"))
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.POST, "id", "updateAddressBook"))
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.DELETE, "id", "deleteAddressBook"))
                .usingEntity(serviceEntity)
                .bind();
 * }
 * can easily be translated into a JAXRS resources by using the rest-jaxrs extension.
 *
 * Request will be delegated to the {@link ServiceEntity}'s method using the information contained within passed
 * {@link ServiceHandler}. Using the example above, a request to:
 * <p>
 *   PUT /createAddressBook/myBook
 * <p>
 * will be mapped on the server side to the ServiceHandler defined as
 * <p>
 * new ServiceHandler(ServiceDefinition.HttpMethod.PUT, "id", "createAddressBook")
 * <p>
 * which will invoke the {@link ServiceEntity}
 * <p>
 *     serviceEntity.createAddressBook(myBook)  
 *
 */
public interface ServiceDefinition {

    /**
     * Represent an HTTP Method
     */
    public enum HttpMethod {
        POST, GET, PUT, DELETE, HEAD
    }

    /**
     * Represent a Content-Type
     */
    public enum Media {
        JSON, XML
    }

    /**
     * Set the {@link ServiceEntity} this service is fronting. All REST requests will eventually be rooted to an instance
     * of @link ServiceEntity}.
     *
     * @param serviceEntity a {@link ServiceEntity}
     * @return the current {@link ServiceDefinition}
     */
    ServiceDefinition usingEntity(ServiceEntity serviceEntity);

    /**
     * Set the url path this {@link ServiceDefinition} is representing.
     * @param path the uri
     * @return the current {@link ServiceDefinition}
     */
    ServiceDefinition withPath(String path);

    /**
     * Add a {@link ServiceHandler} used when mapping request to a {@link ServiceEntity}. This method can be invoked many times.
     * @param serviceHandler a {@link ServiceHandler}
     * @return the current {@link ServiceDefinition}
     */
    ServiceDefinition withHandler(ServiceHandler serviceHandler);

    /**
     * Add a {@link Media} this ServiceDefinition. {@link Media} are used when writing the response and maps the HTTP response's content-type header.
     * @param media {@link Media}
     * @return the current {@link ServiceDefinition}
     */
    ServiceDefinition producing(Media media);

    /**
     * Add a {@link Media} this ServiceDefinition. {@link Media} are used when reading the request and maps the HTTP request's content-type header.
     * @param media {@link Media}
     * @return the current {@link ServiceDefinition}
     */
    ServiceDefinition consuming(Media media);

    /**
     * Return the URL this ServiceDefinition represent.
     * @return the URL this ServiceDefinition represent.
     */
    String path();

    /**
     * Return the {@link ServiceEntity} this ServiceDefinition represent.
     * @return the {@link ServiceEntity} this ServiceDefinition represent.
     */
    ServiceEntity serviceEntity();

    /**
     * Return an unmodifiable {@link List} of {@link ServiceHandler}
     * @return an unmodifiable {@link List} of {@link ServiceHandler}
     */
    List<ServiceHandler> serviceHandlers();

    /**
     * Return an unmodifiable {@link List} of {@link Media}
     * @return an unmodifiable {@link List} of {@link Media}
     */
    List<Media> mediaToConsume();

    /**
     * Return an unmodifiable {@link List} of {@link Media}
     * @return an unmodifiable {@link List} of {@link Media}
     */
    List<Media> mediaToProduce();

    /**
     * Bind the {@link ServiceDefinition}. This operation may generate a new class based on the information this class is
     * holding. The bind operation can be invoked many time and will generate a server side component on every invocation.
     */
    void bind();
}
