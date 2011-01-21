package org.sonatype.rest.spi;

import org.sonatype.rest.api.ServiceDefinition;

/**
 * Generates a resource based on the information a {@link org.sonatype.rest.api.ServiceDefinition} represents.
 *
 * This class is for framework integrator.
 */
public interface ServiceDefinitionGenerator {

    /**
     * Generate a REST resource based on the information a {@link org.sonatype.rest.api.ServiceDefinition} represents.
     * @param serviceDefinition
     */
    void generate(ServiceDefinition serviceDefinition);

}
