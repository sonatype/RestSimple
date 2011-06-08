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
