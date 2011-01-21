package org.sonatype.rest.spi;

/**
 * Bind a resource using the Dependency injection used (Default is Guice).
 */
public interface ResourceBinder {

    /**
     * Bind that class. Usually that method gets invoked to bind dynamically generated classes.
     * @param clazz A {@link Class}
     */
    void bind(Class<?> clazz);

}
