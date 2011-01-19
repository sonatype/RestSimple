package org.sonatype.server.setup;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import org.sonatype.rest.api.ResourceBinder;
import org.sonatype.rest.api.ServiceDefinition;
import org.sonatype.rest.api.ServiceDefinitionGenerator;
import org.sonatype.rest.api.ServiceDefinitionProvider;
import org.sonatype.rest.api.ServiceEntity;
import org.sonatype.rest.impl.JAXRSServiceDefinitionGenerator;
import org.sonatype.rest.impl.JAXRSServiceDefinitionProvider;

public class JaxrsModule extends AbstractModule {

    private final Binder binder;

    public JaxrsModule(Binder binder) {
        this.binder = binder;
    }

    @Override
    protected void configure() {

        bind(ResourceBinder.class).toInstance(new ResourceBinder(){

            @Override
            public void bind(Class<?> clazz) {
                binder.bind(clazz);
            }
        });
        bind(ServiceDefinitionGenerator.class).to(JAXRSServiceDefinitionGenerator.class);
        bind(ServiceEntity.class).to(AddressBookServiceEntity.class);
        bind(ServiceDefinition.class).toProvider(ServiceDefinitionProvider.class);
        bind(ServiceDefinitionProvider.class).to(JAXRSServiceDefinitionProvider.class);
        
    }
}
