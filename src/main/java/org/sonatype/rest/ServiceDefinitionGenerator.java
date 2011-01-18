package org.sonatype.rest;

import java.util.List;

public interface ServiceDefinitionGenerator {

    // Might want to pass a ServiceDefinitionContext here.
    void generate(ServiceDefinition serviceDefinition);

}
