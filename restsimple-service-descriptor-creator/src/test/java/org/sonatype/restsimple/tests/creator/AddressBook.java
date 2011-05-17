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
    private final Map<String, Person> peoplea = new LinkedHashMap<String, Person>();

    private static int idx = 4;

    public AddressBook() {
        peoplea.put("1", new Person("1", "jason@maven.org", "Jason", "van Zyl"));
        peoplea.put("2", new Person("2", "bob@maven.org", "Bob", "McWhirter"));
        peoplea.put("3", new Person("3", "james@maven.org", "James", "Strachan"));
    }

    public Person createPerson(Person person) {
        peoplea.put(person.id, person);
        return person;
    }

    public Person readPerson(String id) {
        return peoplea.get(id);
    }

    public Collection<Person> readPeople() {
        return peoplea.values();
    }

    public Person updatePerson(Person person) {
        System.out.println(person);
        peoplea.put(person.id, person);

        return person;
    }

    public Person deletePerson(String id) {
        return peoplea.remove(id);
    }
}
