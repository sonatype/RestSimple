package org.sonatype.rest.service;

import java.lang.reflect.Method;

import org.sonatype.rest.service.ServiceDefinition.HttpMethod;

public class ServiceHandler 
{
    private HttpMethod httpMethod;
    private String path;
    private Method method;

    public ServiceHandler( HttpMethod httpMethod, String method )
    {        
    }

    public ServiceHandler( HttpMethod httpMethod, String path, String method )
    {
        
    }

    public ServiceHandler( HttpMethod httpMethod, String path, Method method )
    {
        this.httpMethod = httpMethod;
        this.path = path;
        this.method = method;
    }

    public HttpMethod getHttpMethod()
    {
        return httpMethod;
    }
    
    public String getPath()
    {
        return path;
    }

    public Method getMethod()
    {
        return method;
    }
}