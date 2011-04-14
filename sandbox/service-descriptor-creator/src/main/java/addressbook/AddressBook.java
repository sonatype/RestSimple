package org.sonatype.addressbook;

import java.util.Collection;

import org.sonatype.addressbook.model.Person;

public interface AddressBook
{
    //
    // These need to be specified somewhere. Possibly in an annotation or in a configuration
    //
    public final static String APPLICATION = "application";
    public final static String JSON = "vnd.org.sonatype.rest+json";
    public final static String XML = "vnd.org.sonatype.rest+xml";
    
    //
    // I want to be able to take this application interface and generate the service definitions to be used
    // 
    
    //
    // Basic CRUD operations
    //
    
    //POST
    Person createPerson( Person person );
    
    //GET individual
    Person readPerson( String id );
    
    //GET collection
    Collection<Person> readPeople();
    
    //PUT
    Person updatePerson( Person person );
    
    //DELETE
    void deletePerson( String id );
}
