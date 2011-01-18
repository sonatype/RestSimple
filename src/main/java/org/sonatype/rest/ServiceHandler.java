package org.sonatype.rest;

import java.lang.reflect.Method;


public class ServiceHandler {
    private ServiceDefinition.HttpMethod httpMethod;
    private String path;
    private Method method;

    public ServiceHandler(ServiceDefinition.HttpMethod httpMethod, String method) {
    }

    public ServiceHandler(ServiceDefinition.HttpMethod httpMethod, String path, String method) {

    }

    public ServiceHandler(ServiceDefinition.HttpMethod httpMethod, String path, Method method) {
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

    public Method getMethod() {
        return method;
    }

    public ServiceHandler delegatePost(ServiceEntity delegate) {
        return this;
    }

    public ServiceHandler delegateGet(ServiceEntity delegate) {
        return this;
    }

    public ServiceHandler delegatePut(ServiceEntity delegate) {
        return this;
    }

    public ServiceHandler delegateDelete(ServiceEntity delegate) {
        return this;
    }
}