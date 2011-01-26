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
package org.sonatype.rest.api;

/**
 * Transforms the result {@link org.sonatype.rest.api.ServiceEntity} method invocation into a format expected by the client.
 * As an ezample, a {@link org.sonatype.rest.api.ServiceEntity} may produce String, which can be transformed by that class
 * into JSON or XML representation.
 */
public interface ServiceHandlerMediaType<T> {

    /**
     * Transform an Object into another representation
     * @param object an Object i
     * @return a transformed instance of {@link ServiceHandlerMediaType}
     */
    ServiceHandlerMediaType visit(T object);

}
