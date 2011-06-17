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
package org.sonatype.restsimple.api;

/**
 * An abstract representation of a Service.
 *
 * An action is associated with one or more {@link ServiceHandler}. The {@link ServiceHandler} gets mapped from the URI
 * and then its associated Action gets invoked. As an example, a pet action could look like
 * <p><blockquote><pre>
    public class PetstoreAction implements Action&lt;Pet, Pet&gt; {

        public final static String APPLICATION = "application";
        public final static String JSON = "vnd.org.sonatype.rest+json";
        public final static String XML = "vnd.org.sonatype.rest+xml";
        public final static String PET_EXTRA_NAME = "petType";

        private final ConcurrentHashMap&lt;String, Pet&gt; pets = new ConcurrentHashMap&lt;String, Pet&gt;();

        &#64;Override
        public Pet action(ActionContext&lt;Pet&gt; actionContext) throws ActionException {
            Map&lt;String, Collection&lt;String&gt;&gt; headers = actionContext.headers();
            Map&lt;String, Collection&lt;String&gt;&gt; paramsString = actionContext.paramsString();

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
    }</pre></blockquote>
 * You can use the {@link TypedAction} for an implementation of an {@link Action}
 *
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
