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

import org.sonatype.restsimple.api.Action;
import org.sonatype.restsimple.api.ActionContext;
import org.sonatype.restsimple.api.ActionException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AddressBookAction implements Action<AddressBookMediaType, String> {

    public final static String APPLICATION = "application";
    public final static String JSON = "vnd.org.sonatype.rest+json";
    public final static String XML = "vnd.org.sonatype.rest+xml";

    private final ConcurrentHashMap<String, Collection<String>> book = new ConcurrentHashMap<String, Collection<String>>();

    public AddressBookAction() {
    }

    @Override
    public AddressBookMediaType action(ActionContext<String> actionContext) throws ActionException {
        String addressBookName = actionContext.pathValue();
        switch (actionContext.method()) {
            case GET:
                Collection<String> list = book.get(addressBookName);

                if (list != null) {
                    StringBuilder b = new StringBuilder();
                    for(String s: list) {
                        b.append(s);
                        b.append(" - ");
                    }
                    return new AddressBookMediaType(b.toString());
                } else {
                    throw new IllegalStateException("No address book have been created for " + addressBookName);
                }
            case POST:
                list = book.get(addressBookName);

                if (list == null) {
                    throw new IllegalStateException("No address book have been created for " + addressBookName);
                }

                if (actionContext.formParams().size() > 0) {
                    for (Map.Entry<String,Collection<String>> e : actionContext.formParams().entrySet()) {
                        list.addAll(e.getValue());
                    }
                } else {
                    list.add(actionContext.get());
                }

                book.put(addressBookName, list);
                return new AddressBookMediaType("posted");
            case PUT:
                book.put(addressBookName, new ArrayList<String>());
                return new AddressBookMediaType("updated");
            case DELETE:
                book.remove(addressBookName);
                return new AddressBookMediaType("deleted");
            case HEAD:
                break;

            default:
                ;;
        }
        return new AddressBookMediaType("invalid-state");
    }
}
    