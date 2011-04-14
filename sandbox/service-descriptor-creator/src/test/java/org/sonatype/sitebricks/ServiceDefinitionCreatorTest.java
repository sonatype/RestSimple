package org.sonatype.sitebricks;

import javax.inject.Inject;

import org.sonatype.addressbook.AddressBook;
import org.sonatype.guice.bean.containers.InjectedTestCase;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.creator.MethodBasedServiceDefintionCreator;

public class ServiceDefinitionCreatorTest
    extends InjectedTestCase
{
    @Inject 
    private MethodBasedServiceDefintionCreator serviceDefintionCreator;
    
    public void testServiceDefinitionCreator()
        throws Exception
    {
        ServiceDefinition serviceDefinition = serviceDefintionCreator.create( AddressBook.class );
        System.out.println( serviceDefinition );
    }
}

