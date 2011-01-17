package org.sonatype.rest.service;

public interface ServiceDefinition
{
    enum HttpMethod {
        POST, GET, PUT, DELETE        
    }
    
    enum Media {
        JSON, XML
    }
    
    ServiceDefinition usingDelegate( Object d );
    Object getDelegate();
    
    ServiceDefinition withPath( String s );
    String getBasePath();
    
    ServiceDefinition withHandler( ServiceHandler mapping );
    
    ServiceDefinition producing( Media media );
    
    ServiceDefinition consuming( Media media );
}
