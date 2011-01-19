package org.sonatype.server.setup;

import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.sonatype.rest.impl.JAXRSServiceDefinitionGenerator;
import org.sonatype.rest.impl.JAXRSServiceDefinitionProvider;
import org.sonatype.rest.api.ServiceDefinition;
import org.sonatype.rest.api.ServiceDefinitionGenerator;
import org.sonatype.rest.api.ServiceDefinitionProvider;
import org.sonatype.rest.api.ServiceEntity;

public class RestDeflectorServletModule extends ServletModule {

    @Override
    protected void configureServlets() {

        bind(ServiceEntity.class).to(AddressBookServiceEntity.class);

//        // First, bind our Service Provider
//        bind(ServiceDefinitionProvider.class).to(JAXRSServiceDefinitionProvider.class);
//
//        bind(ServiceDefinition.class).toProvider(ServiceDefinitionProvider.class);
//
//        // Next, Define our Resource Generator
//        bind(ServiceDefinitionGenerator.class).toInstance(new JAXRSServiceDefinitionGenerator(binder()));
//
//        //Finally, bind our implementation
//        bind(ServiceDefinitionJAXRSTest.class).asEagerSingleton();

        install(new JaxrsModule());


        serve("/*").with(GuiceContainer.class);
        
    }

}