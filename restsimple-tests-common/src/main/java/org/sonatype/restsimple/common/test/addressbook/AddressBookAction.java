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
package org.sonatype.restsimple.common.test.addressbook;

import org.sonatype.restsimple.api.ActionContext;
import org.sonatype.restsimple.api.TypedAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AddressBookAction extends TypedAction<AddressBook, String> {

    public final static String APPLICATION = "application";
    public final static String JSON = "vnd.org.sonatype.rest+json";
    public final static String XML = "vnd.org.sonatype.rest+xml";

    private final ConcurrentHashMap<String, Collection<String>> book = new ConcurrentHashMap<String, Collection<String>>();

    public AddressBookAction() {
    }

    @Override
    public String toString() {
        return "AddressBookAction";
    }

    @Override
    public AddressBook get(ActionContext<String> actionContext) {
        String addressBookName = actionContext.pathParams().get("ad");
        Collection<String> list = book.get(addressBookName);

        if (list != null) {
            StringBuilder b = new StringBuilder();
            for (String s : list) {
                b.append(s);
                b.append(" - ");
            }
            return new AddressBook(b.toString());
        } else {
            throw new IllegalStateException("No address book have been created for " + addressBookName);
        }
    }

    @Override
    public AddressBook post(ActionContext<String> actionContext) {
        String addressBookName = actionContext.pathParams().get("ad");

        Collection<String> list = book.get(addressBookName);

        if (list == null) {
            throw new IllegalStateException("No address book have been created for " + addressBookName);
        }

        if (actionContext.paramsString().size() > 0) {
            for (Map.Entry<String, Collection<String>> e : actionContext.paramsString().entrySet()) {
                list.addAll(e.getValue());
            }
        } else {
            list.add(actionContext.get());
        }

        book.put(addressBookName, list);
        return new AddressBook("posted");
    }

    @Override
    public AddressBook put(ActionContext<String> actionContext) {
        String addressBookName = actionContext.pathParams().get("ad");

        book.put(addressBookName, new ArrayList<String>());
        return new AddressBook("updated");
    }

    @Override
    public AddressBook delete(ActionContext<String> actionContext) {
        String addressBookName = actionContext.pathParams().get("ad");

        book.remove(addressBookName);
        return new AddressBook("deleted");
    }

}
    