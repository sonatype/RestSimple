package org.sonatype.rest;

import com.google.inject.Inject;

public class JAXRSServiceDefinitionProvider implements ServiceDefinitionProvider{

    @Inject
    public ServiceDefinitionGenerator generator;

    @Override
    public ServiceDefinition get() {
        return new DefaultServiceDefinition(generator);
    }
}
