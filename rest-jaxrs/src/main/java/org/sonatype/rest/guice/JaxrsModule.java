package org.sonatype.rest.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import org.sonatype.rest.spi.ResourceBinder;
import org.sonatype.rest.api.ServiceDefinition;
import org.sonatype.rest.spi.ServiceDefinitionGenerator;
import org.sonatype.rest.spi.ServiceDefinitionProvider;
import org.sonatype.rest.spi.ServiceHandlerMapper;
import org.sonatype.rest.impl.JAXRSServiceDefinitionGenerator;
import org.sonatype.rest.impl.JAXRSServiceDefinitionProvider;

public class JaxrsModule extends AbstractModule {

    private final Binder binder;

    public JaxrsModule(Binder binder) {
        this.binder = binder;
    }

    @Override
    protected void configure() {
        final ServiceHandlerMapper mapper = new ServiceHandlerMapper();
        bind(ServiceHandlerMapper.class).toInstance(mapper);
        
        bind(ResourceBinder.class).toInstance(new ResourceBinder(){

            @Override
            public void bind(Class<?> clazz) {
                binder.bind(ServiceHandlerMapper.class).toInstance(mapper);
                binder.bind(clazz);
            }
        });

        
        bind(ServiceDefinitionGenerator.class).to(JAXRSServiceDefinitionGenerator.class);
        bind(ServiceDefinition.class).toProvider(ServiceDefinitionProvider.class);
        bind(ServiceDefinitionProvider.class).to(JAXRSServiceDefinitionProvider.class);
        
    }
}
