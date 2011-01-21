package org.sonatype.rest.api;

/**
 * Represent how a REST resource handles requests. A {@link ServiceHandler} is used when mapping the request to
 * a {@link ServiceEntity}
 *
 * TODO: make me a builder
 */
public class ServiceHandler {
    private final ServiceDefinition.HttpMethod httpMethod;
    private final String path;
    private final String method;

    /**
     * Create a new {@link ServiceHandler}
     * @param httpMethod an {@link ServiceDefinition.HttpMethod}
     * @param methodToInvoke a method's name used to invoke a {@link ServiceEntity}
     */
    public ServiceHandler(ServiceDefinition.HttpMethod httpMethod, String methodToInvoke) {
        this(httpMethod, null, methodToInvoke);
    }

    /**
     * Create a new {@link ServiceHandler}
     * @param httpMethod an {@link ServiceDefinition.HttpMethod}
     * @path path a uri used to map the resource to this {@link ServiceHandler}
     * @param methodToInvoke a methodToInvoke's name used to invoke a {@link ServiceEntity}
     */
    public ServiceHandler(ServiceDefinition.HttpMethod httpMethod, String path, String methodToInvoke) {
        this.httpMethod = httpMethod;
        this.path = path;
        this.method = methodToInvoke;
    }

    /**
     * Return the current {@link ServiceDefinition.HttpMethod}
     * @return {@link ServiceDefinition.HttpMethod}
     */
    public ServiceDefinition.HttpMethod getHttpMethod() {
        return httpMethod;
    }

    /**
     * Retunr the URI used to map this {@link ServiceHandler}
     * @return
     */
    public String getPath() {
        return path;
    }

    /**
     * Return the HTTP method.
     * @return
     */
    public String getMethod() {
        return method;
    }

}