package org.sonatype.addressbook.model;

public class Person
{
    public String id;
    public String email;
    public String firstName;
    public String lastName;
    
    //
    // Required by entity mapper and anything that needs a default constructor
    //
    public Person()
    {        
    }
    
    public Person( String id, String email, String firstName, String lastName )
    {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String toString()
    {
        return firstName + " " + lastName + " " + email;
    }
}
