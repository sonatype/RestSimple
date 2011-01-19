package org.sonatype.server.setup;

import com.google.inject.Binder;
import com.google.inject.servlet.ServletModule;
import org.sonatype.rest.JAXRSServiceDefinitionGenerator;
import org.sonatype.rest.JAXRSServiceDefinitionProvider;
import org.sonatype.rest.ServiceDefinition;
import org.sonatype.rest.ServiceDefinitionGenerator;
import org.sonatype.rest.ServiceDefinitionProvider;
import org.sonatype.rest.ServiceEntity;
import org.sonatype.server.resources.ServiceDefinitionTest;

public class RestDeflectorServletModule extends ServletModule {

    @Override
    protected void configureServlets() {

        bind(ServiceEntity.class).to(AddressBookServiceEntity.class);

        // First, bind our Service Provider
        bind(ServiceDefinitionProvider.class).to(JAXRSServiceDefinitionProvider.class);

        // Next, Define our Resource Generator
        bind(ServiceDefinitionGenerator.class).toInstance(new JAXRSServiceDefinitionGenerator(binder()));

        // Finally bind our Provider
        bind(ServiceDefinition.class).toProvider(JAXRSServiceDefinitionProvider.class);

        //Finally, bind our implementation
        bind(ServiceDefinitionTest.class).asEagerSingleton();

    }

}