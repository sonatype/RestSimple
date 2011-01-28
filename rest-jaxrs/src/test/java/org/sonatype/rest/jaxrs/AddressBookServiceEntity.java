/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.sonatype.rest.jaxrs;

import org.sonatype.rest.api.ServiceEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class AddressBookServiceEntity implements ServiceEntity {

    public final static String APPLICATION = "application";
    public final static String JSON = "org.sonatype.rest.tests.addressBook+json";
    public final static String XML = "org.sonatype.rest.tests.addressBook+xml";

    private final ConcurrentHashMap<String, List<String>> book = new ConcurrentHashMap<String, List<String>>();

    public String createAddressBook(String id) {
        book.put(id, new ArrayList<String>());
        return "created";
    }

    public List getAddressBook(String id) {
        return book.get(id);
    }

    public String updateAddressBook(String id, String value) {
        List<String> list = book.get(id);
        
        if (list == null) {
            throw new IllegalStateException("No address book have been created for " + id);
        }

        list.add(value);
        book.put(id, list);
        return "updated";
    }

    public String updateAddressBook(String id, String value, String value2) {
        List<String> list = book.get(id);

        if (list == null) {
            throw new IllegalStateException("No address book have been created for " + id);
        }

        list.add(value);
        list.add(value2);
        book.put(id, list);
        return "updated";
    }

    public String deleteAddressBook(String id) {
        book.remove(id);
        return "updated";
    }

    @Override
    public List<String> version() {
        ArrayList<String> list = new ArrayList<String>();
        list.add(APPLICATION + "/" + JSON);
        list.add(APPLICATION + "/" + XML);
        return list;
    }
}
    