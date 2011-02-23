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
package org.sonatype.restsimple.tests;

import org.sonatype.restsimple.api.Action;
import org.sonatype.restsimple.api.ActionContext;
import org.sonatype.restsimple.api.ActionException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AddressBookAction implements Action<String> {

    public final static String APPLICATION = "application";
    public final static String JSON = "vnd.org.sonatype.rest+json";
    public final static String XML = "vnd.org.sonatype.rest+xml";

    private final ConcurrentHashMap<String, Collection<String>> book = new ConcurrentHashMap<String, Collection<String>>();

    public AddressBookAction() {
    }

    @Override
    public String action(ActionContext actionContext) throws ActionException {

        String response = "";
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
                    return b.toString();
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
                    InputStream is = actionContext.inputStream();
                    StringBuffer b = new StringBuffer();
                    int nRead = 0;
                    while (nRead > -1) {
                        try {
                            byte[] bytes = new byte[1];
                            nRead = is.read(bytes);
                            if (nRead > -1) {
                                b.append(new String(bytes));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    list.add(b.toString());
                }
                                
                book.put(addressBookName, list);
                return "updated";
            case PUT:
                book.put(addressBookName, new ArrayList<String>());
                return "updated";
            case DELETE:
                book.remove(addressBookName);
                return "updated";
            case HEAD:
                break;

            default:
                ;;
        }
        return response;
    }
}
    