package org.sonatype.restsimple.creator;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.addressbook.AddressBook;
import org.sonatype.addressbook.model.Person;
import org.sonatype.restsimple.api.Action;
import org.sonatype.restsimple.api.ActionContext;
import org.sonatype.restsimple.api.ActionException;
import org.sonatype.restsimple.api.DefaultServiceDefinition;
import org.sonatype.restsimple.api.DeleteServiceHandler;
import org.sonatype.restsimple.api.GetServiceHandler;
import org.sonatype.restsimple.api.MediaType;
import org.sonatype.restsimple.api.PostServiceHandler;
import org.sonatype.restsimple.api.PutServiceHandler;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.sitebricks.AddressBookMediaType;

// ------------------------------
// Method   URL        Action
// ------------------------------
// POST     /users     create
// GET      /users     read
// GET      /users/23  read
// PUT      /users/23  update
// DESTROY  /users/23  delete

@Named
@Singleton
public class MethodBasedServiceDefintionCreator
    implements ServiceDefinitionCreator
{
    public ServiceDefinition create( Class<?> application )
    {
        ServiceDefinition serviceDefinition = new DefaultServiceDefinition();

        Method[] methods = application.getDeclaredMethods();
        //
        // Using a simple method of looking at the method names we will map URIs to method invocations.
        //
        for ( Method method : methods )
        {
            if ( method.getName().startsWith( "create" ) )
            {
                serviceDefinition.withHandler( new PostServiceHandler( "/users", new Action<AddressBookMediaType,String>()
                {
                    public AddressBookMediaType action( ActionContext<String> actionContext )
                        throws ActionException
                    {
                        return null;
                    }
                } ) );
            }

            if ( method.getName().startsWith( "read" ) )
            {
                Type[] types = method.getGenericParameterTypes();

                if ( types.length == 0 )
                {
                    // Collection
                    //
                    serviceDefinition.withHandler( new GetServiceHandler( "/users", new Action<AddressBookMediaType,String>()
                                                                          {
                        public AddressBookMediaType action( ActionContext<String> actionContext )
                            throws ActionException
                        {
                            return null;
                        }
                    } ) );
                }
                else if ( types.length == 1 )
                {
                    // Individual
                    //
                    serviceDefinition.withHandler( new GetServiceHandler( "/user", new Action<AddressBookMediaType,String>()
                                                                          {
                        public AddressBookMediaType action( ActionContext<String> actionContext )
                            throws ActionException
                        {
                            return null;
                        }
                    } ) );
                }
            }

            if ( method.getName().startsWith( "update" ) )
            {
                serviceDefinition.withHandler( new PutServiceHandler( "/users", new Action<AddressBookMediaType,String>()
                {
                    public AddressBookMediaType action( ActionContext<String> actionContext )
                        throws ActionException
                    {
                        return null;
                    }
                } ) );
            }

            if ( method.getName().startsWith( "delete" ) )
            {
                serviceDefinition.withHandler( new DeleteServiceHandler( "/user", new Action<AddressBookMediaType,String>()
                {
                    public AddressBookMediaType action( ActionContext<String> actionContext )
                        throws ActionException
                    {
                        return null;
                    }
                } ) );
            }
        }

        //
        // Are we really going to produce one media type and not consume it?
        //

        serviceDefinition.producing( new MediaType( AddressBook.APPLICATION, AddressBook.JSON ) );
        serviceDefinition.producing( new MediaType( AddressBook.APPLICATION, AddressBook.XML ) );

        serviceDefinition.consuming( MediaType.JSON );
        serviceDefinition.consuming( MediaType.XML );

        return serviceDefinition;
    }
}
