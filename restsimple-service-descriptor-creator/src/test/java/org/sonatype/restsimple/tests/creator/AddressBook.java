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

public interface AddressBook {
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
    Person createPerson(Person person);

    //GET individual
    Person readPerson(String id);

    //GET collection
    Collection<Person> readPeople();

    //PUT
    Person updatePerson(Person person);

    //DELETE
    void deletePerson(String id);
}
