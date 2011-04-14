package org.sonatype.addressbook;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.addressbook.model.Person;

// I need a way to get an ID from the source possibly

@Named
@Singleton
public class DefaultAddressBook
    implements AddressBook
{
    private Map<String, Person> people;

    private static int idx = 4;

    public DefaultAddressBook()
    {
        people = new LinkedHashMap<String, Person>();

        people.put( "1", new Person( "1", "jason@maven.org", "Jason", "van Zyl" ) );
        people.put( "2", new Person( "2", "bob@maven.org", "Bob", "McWhirter" ) );
        people.put( "3", new Person( "3", "james@maven.org", "James", "Strachan" ) );
    }

    public Person createPerson( Person person )
    {
        System.out.println( "adding person: " + person );
        person.id = Integer.toString( idx++ );
        people.put( person.id, person );
        return person;
    }

    public Person readPerson( String id )
    {
        return people.get( id );
    }

    public Collection<Person> readPeople()
    {
        return people.values();
    }

    public Person updatePerson( Person person )
    {
        //
        // Now we have an entity with only updated fields and the id so we need an efficient way to know what
        // changed and only apply those changes.
        //
        // We should run the experiment as to what's faster:
        //
        // 1. sending over only the changed fields and then inspecting the object on the server side to apply
        //    the changes. this would actually work better from the summary view because I won't have the complete object
        //
        // 2. sending over the whole object and simple updating the whole object.
        //
        System.out.println( person );
        people.put( person.id, person );
        
        return person;
    }

    public void deletePerson( String id )
    {
        //
        // Need to know that we successfully remove the entity from the store and send the appropriate
        // message back to the user.
        //
        people.remove( id );
    }
}
