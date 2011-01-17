package org.sonatype.rest.service;

import java.util.ArrayList;
import java.util.List;

public class DefaultServiceDefinition
    implements ServiceDefinition
{
    private String basePath;
    private Object delegate;
    private List<ServiceHandler> mappings;
    //
    // This allows a particular method to be selected based on a parameter in the request
    //
    // @Select("metadata")
    // @Post
    // @Get
    // @At( "/:id" ) @Get
    // @At( "/:id" ) @Put
    // @At( "/:id" ) @Delete
    // @Get("form")
           
    public DefaultServiceDefinition()
    {        
    }
    
    public DefaultServiceDefinition( String basePath, Object delegate, List<ServiceHandler> mappings )
    {
        this.basePath = basePath;
        this.delegate = delegate;
        this.mappings = mappings;
    }

    public ServiceDefinition withPath( String basePath )
    {
        this.basePath = basePath;
        return this;
    }
    
    public String getBasePath()
    {
        return basePath;
    }
    
    public ServiceDefinition usingDelegate( Object delegate )
    {
        this.delegate = delegate;
        return this;
    }
    
    public Object getDelegate()
    {
        return delegate;
    }

    public ServiceDefinition withHandler( ServiceHandler mapping )
    {
        if ( mappings == null )
        {
            mappings = new ArrayList<ServiceHandler>();
        }
        mappings.add(  mapping );        
        return this;
    }
    
    public List<ServiceHandler> getMappings()
    {
        return mappings;
    }

    public ServiceDefinition producing( Media media )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public ServiceDefinition consuming( Media media )
    {
        // TODO Auto-generated method stub
        return null;
    }
}
