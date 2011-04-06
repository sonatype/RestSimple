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
package org.sonatype.restsimple.spi;

/**
 * Bind and install a resource using dependency injection (Default is Guice). This class can also be used
 * to manually bind or install a resource if dependency injection is not supported.
 */
public interface ResourceModuleConfig<T> {

    /**
     * Bind an instance to it's class.
     * @param clazz A Class
     * @param instance An instance to bind to
     */
    <A> void bindToInstance(Class<A> clazz, A instance);

    /**
     * Bind static classes
     * @param clazz  A class
     * @param clazz2 A class to bind to
     */
    <A> void bindTo(Class<A> clazz, Class<? extends A> clazz2);

    /**
     * Bind that class. Usually that method gets invoked to bind dynamically generated classes.
     * @param clazz A {@link Class}
     */
    void bind(Class<?> clazz);

    /**
     * Install a module
     * @param module
     */
    void install(T module);
    
}
