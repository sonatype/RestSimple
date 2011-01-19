package org.sonatype.rest.api;

public interface ServiceDefinitionGenerator {

    // Might want to pass a ServiceDefinitionContext here.
    void generate(ServiceDefinition serviceDefinition);

}
