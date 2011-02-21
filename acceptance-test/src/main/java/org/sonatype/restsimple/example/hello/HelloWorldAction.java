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
package org.sonatype.restsimple.example.hello;

import org.sonatype.restsimple.api.Action;
import org.sonatype.restsimple.api.ActionContext;

public class HelloWorldAction implements Action {
    public final static String APPLICATION = "application";
    public final static String TXT = "vnd.org.sonatype.rest+txt";
    public final static String XML = "vnd.org.sonatype.rest+xml";
    public final static String JSON = "vnd.org.sonatype.rest+json";
    public final static String HTML = "vnd.org.sonatype.rest+html";

    public String sayPlainTextHello(String name) {
        return "Hello RestSimple " + name;
    }

    public String sayPlainXmlHello(String name) {
        return name;
    }

    public String sayPlainJsonHello(String name) {
        return name;
    }

    public String sayPlainHtmlHello(String name) {
        return "<html> " + "<title>" + "Hello RestSimple " + name + "</title>"
                + "<body><h1>" + "Hello Jersey" + "</body></h1>" + "</html> ";
    }

    @Override
    public Object action(ActionContext actionContext) {

        if (actionContext.pathName().equalsIgnoreCase("sayPlainTextHello")) {
            return sayPlainTextHello(actionContext.pathValue());
        } else if (actionContext.pathName().equalsIgnoreCase("sayPlainHtmlHello")) {
            return sayPlainHtmlHello(actionContext.pathValue());
        } else if (actionContext.pathName().equalsIgnoreCase("sayPlainXmlHello")) {
            return sayPlainXmlHello(actionContext.pathValue());
        } else {
            return sayPlainJsonHello(actionContext.pathValue());
        }
    }
}
