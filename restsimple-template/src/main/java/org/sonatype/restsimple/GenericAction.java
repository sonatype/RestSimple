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
package org.sonatype.restsimple;

import org.sonatype.restsimple.api.Action;
import org.sonatype.restsimple.api.ActionContext;
import org.sonatype.restsimple.api.ActionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GenericAction implements Action<String,Integer> {

    private final Object object;
    private final Method method;
       
    public GenericAction(Object object, Method method) {
        this.object = object;
        this.method = method;
    }

    @Override
    public String action(final ActionContext<Integer> objectActionContext) throws ActionException {
        try {
            return (String) method.invoke(object, new Object[] {getActionType(objectActionContext)});
        } catch (IllegalAccessException e) {
            throw new ActionException(e);
        } catch (InvocationTargetException e) {
            throw new ActionException(e);
        }
    }

    private final static Object getActionType(ActionContext<Integer> objectActionContext) {
        Object o = objectActionContext.get();
        if (String.class.isAssignableFrom(o.getClass()) && String.class.cast(o).equalsIgnoreCase("")) {
            o = objectActionContext.pathValue();
        }
        return o;
    }
}
