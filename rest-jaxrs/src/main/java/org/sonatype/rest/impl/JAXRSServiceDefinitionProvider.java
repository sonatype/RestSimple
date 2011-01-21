package org.sonatype.rest.impl;

import com.google.inject.Inject;
import org.sonatype.rest.api.ServiceDefinition;
import org.sonatype.rest.api.ServiceDefinitionGenerator;
import org.sonatype.rest.api.ServiceDefinitionProvider;
import org.sonatype.rest.api.ServiceHandlerMapper;

public class JAXRSServiceDefinitionProvider implements ServiceDefinitionProvider {

    @Inject
    public ServiceDefinitionGenerator generator;

    @Inject
    ServiceHandlerMapper mapper;

    @Override
    public ServiceDefinition get() {
        return new DefaultServiceDefinition(generator, mapper);
    }
}
