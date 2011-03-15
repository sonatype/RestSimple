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
package org.sonatype.restsimple.example.petstore;

import org.sonatype.restsimple.api.Action;
import org.sonatype.restsimple.api.ActionContext;
import org.sonatype.restsimple.api.ActionException;

public class PetstoreAction implements Action<Pet,Pet> {

    public final static String APPLICATION = "application";
    public final static String JSON = "vnd.org.sonatype.rest+json";
    public final static String XML = "vnd.org.sonatype.rest+xml";
    
    @Override
    public Pet action(ActionContext<Pet> actionContext) throws ActionException {

        switch (actionContext.method()) {
             case GET:
                return actionContext.get();
             case POST:
                 return actionContext.get();
             default:
                 throw new ActionException(405);
        }
    }
}
