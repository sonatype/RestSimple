package org.sonatype.rest.impl;

import com.google.inject.Inject;
import org.sonatype.rest.api.ServiceDefinition;
import org.sonatype.rest.api.ServiceDefinitionGenerator;
import org.sonatype.rest.api.ServiceDefinitionProvider;

public class JAXRSServiceDefinitionProvider implements ServiceDefinitionProvider {

    @Inject
    public ServiceDefinitionGenerator generator;

    @Override
    public ServiceDefinition get() {
        return new DefaultServiceDefinition(generator);
    }
}
