/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.restsimple.tests.creator;

import org.sonatype.restsimple.tests.creator.model.Person;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class AddressBook {
    private Map<String, Person> people;

    private static int idx = 4;

    public AddressBook() {
        people = new LinkedHashMap<String, Person>();

        people.put("1", new Person("1", "jason@maven.org", "Jason", "van Zyl"));
        people.put("2", new Person("2", "bob@maven.org", "Bob", "McWhirter"));
        people.put("3", new Person("3", "james@maven.org", "James", "Strachan"));
    }

    public Person createPerson(Person person) {
        System.out.println("adding person: " + person);
        person.id = Integer.toString(idx++);
        people.put(person.id, person);
        return person;
    }

    public Person readPerson(String id) {
        return people.get(id);
    }

    public Collection<Person> readPeople() {
        return people.values();
    }

    public Person updatePerson(Person person) {
        System.out.println(person);
        people.put(person.id, person);

        return person;
    }

    public Person deletePerson(String id) {
        return people.remove(id);
    }
}
