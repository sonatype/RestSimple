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
package org.sonatype.restsimple.example.hello;

import org.sonatype.restsimple.api.Action;
import org.sonatype.restsimple.api.ActionContext;
import org.sonatype.restsimple.api.ActionException;

import java.util.Map;

public class HelloWorldAction implements Action<Object, String> {
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
    public Object action(ActionContext<String> actionContext) throws ActionException {

        Map<String,String> pathParams = actionContext.pathParams();
        if (pathParams.containsKey("sayPlainTextHello")) {
            return sayPlainTextHello(pathParams.get("name"));
        } else if (pathParams.containsKey("sayPlainHtmlHello")) {
            return sayPlainHtmlHello(pathParams.get("name"));
        } else if (pathParams.containsKey("sayPlainXmlHello")) {
            return new HelloWorld(sayPlainXmlHello(pathParams.get("name")));
        } else {
            return new HelloWorld(sayPlainJsonHello(pathParams.get("name")));
        }
    }
}
