package org.sonatype.rest;

import java.lang.reflect.Method;

/**
 * TODO: Make me a builder
 */
public class ServiceHandler {
    private final ServiceDefinition.HttpMethod httpMethod;
    private final String path;
    private final String method;

    public ServiceHandler(ServiceDefinition.HttpMethod httpMethod, String method) {
        this(httpMethod, null, method);
    }

    public ServiceHandler(ServiceDefinition.HttpMethod httpMethod, String path, String method) {
        this.httpMethod = httpMethod;
        this.path = path;
        this.method = method;
    }

    public ServiceDefinition.HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }

}