/*
 * Copyright (c) 2011 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.restsimple.api;

/**
 * An abstract representation of a Service.
 *
 * An action is associated with one or more {@link ServiceHandler}. The {@link ServiceHandler} gets mapped from the URI
 * and then its associated Action gets invoked. As an example, a pet action could look like
 * {@code

    public class PetstoreAction implements Action<Pet, Pet> {

        public final static String APPLICATION = "application";
        public final static String JSON = "vnd.org.sonatype.rest+json";
        public final static String XML = "vnd.org.sonatype.rest+xml";
        public final static String PET_EXTRA_NAME = "petType";

        private final ConcurrentHashMap<String, Pet> pets = new ConcurrentHashMap<String, Pet>();

        @Override
        public Pet action(ActionContext<Pet> actionContext) throws ActionException {
            Map<String, Collection<String>> headers = actionContext.headers();
            Map<String, Collection<String>> queryStrings = actionContext.queryStrings();

            switch (actionContext.method()) {
                case GET:
                    Pet pet = pets.get(actionContext.pathValue());
                    return pet;
                case DELETE:
                    return pets.remove(actionContext.pathValue());
                case POST:
                    pet = actionContext.get();
                    pets.put(actionContext.pathValue(), pet);
                    return pet;
                default:
                    throw new ActionException(405);
            }
        }
    }
  
   }
 * The de-serialization of the Request's body is always performed before the {@link Action#action} and the result can
 * always be retrieved using {@link org.sonatype.restsimple.api.ActionContext#get()} The returned value of the
 * {@link Action#action} will be serialized based on the information represented by the associated {@link ServiceHandler}.
 *
 * This class is not thread safe.
 */
public interface Action<T, U> {

    /**
     * Execute an action. An action can be anything.
     * @param actionContext an {@link ActionContext}
     * @return T a response to be serialized
     */
    public T action(ActionContext<U> actionContext) throws ActionException;

}
