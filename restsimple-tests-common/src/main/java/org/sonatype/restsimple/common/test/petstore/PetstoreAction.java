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
package org.sonatype.restsimple.common.test.petstore;

import org.sonatype.restsimple.api.ActionContext;
import org.sonatype.restsimple.api.TypedAction;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PetstoreAction extends TypedAction<Pet, Pet> {

    public final static String APPLICATION = "application";
    public final static String JSON = "vnd.org.sonatype.rest+json";
    public final static String TEXT = "vnd.org.sonatype.rest+txt";
    public final static String PET_EXTRA_NAME = "petType";
    private final ConcurrentHashMap<String, Pet> pets = new ConcurrentHashMap<String, Pet>();

    @Override
    public Pet get(ActionContext<Pet> actionContext) {
        String pathValue = actionContext.pathParams().get("pet");
        Map<String, Collection<String>> headers = actionContext.headers();

        Pet pet = pets.get(pathValue);
        if (pet != null) {

            if (headers.size() > 0) {
                for (Map.Entry<String, Collection<String>> e : headers.entrySet()) {
                    if (e.getKey().equals("Cookie")) {
                        pet.setName(pet.getName() + "--" + e.getValue().iterator().next());
                        break;
                    }
                }
            }
        }
        return pet;
    }

    @Override
    public Pet post(ActionContext<Pet> actionContext) {
        Map<String, Collection<String>> headers = actionContext.headers();
        Map<String, Collection<String>> queryStrings = actionContext.paramsString();
        Map<String, Collection<String>> matrixParams = actionContext.matrixString();

        String pathValue = actionContext.pathParams().get("pet");

        Pet pet = actionContext.get();
        if (headers.size() > 0) {
            for (Map.Entry<String, Collection<String>> e : headers.entrySet()) {
                if (e.getKey().equals(PET_EXTRA_NAME)) {
                    pet.setName(pet.getName() + "--" + e.getValue().iterator().next());
                    break;
                }
            }
        }

        if (queryStrings.size() > 0) {
            for (Map.Entry<String, Collection<String>> e : queryStrings.entrySet()) {
                if (e.getKey().equals(PET_EXTRA_NAME)) {
                    pet.setName(pet.getName() + "--" + e.getValue().iterator().next());
                    break;
                }
            }
        }

        if (matrixParams.size() > 0) {
            for (Map.Entry<String, Collection<String>> e : matrixParams.entrySet()) {
                if (e.getKey().equals(PET_EXTRA_NAME)) {
                    pet.setName(pet.getName() + "--" + e.getValue().iterator().next());
                    break;
                }
            }
        }

        String value = pathValue;
        int matrixPos = value.indexOf(";");
        if (matrixPos > 0) {
            value = pathValue.substring(0, matrixPos);
        }

        pets.put(value, pet);
        return pet;
    }

    @Override
    public Pet delete(ActionContext<Pet> actionContext) {
        String pathValue = actionContext.pathParams().get("pet");

        return pets.remove(pathValue);
    }
}
