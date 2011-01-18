package org.sonatype.rest.service;

import junit.framework.TestCase;

public class ServiceDefinitionTest
    extends TestCase
{
    public void testServiceDefinition() {
        //
        // We need to deal with
        //
        // - error handling
        // - parameter validation
        // - decoration of the raw (un)marshalling for particular view types 
        //   - like ExtJS which require mutations of standard patterns to include metadata for its widgets to help with things like paging, or databinding
        // - my goal here is to produce these by using some convention. i realise this block of code is getting as long as making an actua resource :-)
        // - version of services and how to interoperate between different versions, so negotiation of version in the header
        //
        // requirement: adding a definition dynamically should work, insert, activate and presto!
        //
        ServiceDefinition serviceDefinition = new DefaultServiceDefinition()
            //
            // This is the primary path of the resource. Handlers can use this path for HTTP operations, but may also 
            // append sub paths
            //
            .withPath( "/service/:type" )
            //
            // This handles the case when all handlers for the service are delegating to a single instance, may not be ideal if you want
            // to route different paths of the resource to different instances ...
            //
            .usingDelegate( new DefaultAddressBook() )
            //
            // Would it ever make sense to have handlers for a server negotiate different media types?
            //
            // Handling automatically the version here automatically
            //
            .producing( Media.JSON )
            .producing( Media.XML )
            .consuming( Media.JSON )
            .consuming( Media.XML )
            //
            // This would probably be better encapsulted to include the delegate and media types possibly... Maybe we
            // should just set a default delegate and default media types...
            //
            // These are MVEL expressions but we probably want something more generic here, this is just what I'm using at
            // the moment for prototyping. Handlers should really just say what HTTP method they deal with, an optional
            // sub path, and some generic invocation.
            //
            .withHandler( new ServiceHandler( HttpMethod.POST, "createPerson(entity)" ) )                
            .withHandler( new ServiceHandler( HttpMethod.GET, "/:id", "readPerson(id)" ) )                
            .withHandler( new ServiceHandler( HttpMethod.GET, "readPeople()" ) )                
            .withHandler( new ServiceHandler( HttpMethod.PUT, "/:id", "updatePerson(entity)" ) )                
            .withHandler( new ServiceHandler( HttpMethod.DELETE, "/:id", "deletePerson(id)" ) );                          
    }
}
