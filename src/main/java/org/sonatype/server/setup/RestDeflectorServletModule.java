package org.sonatype.server.setup;

import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.sonatype.rest.api.ServiceDefinition;
import org.sonatype.rest.api.ServiceDefinitionGenerator;
import org.sonatype.rest.api.ServiceDefinitionProvider;
import org.sonatype.rest.api.ServiceEntity;
import org.sonatype.rest.api.ServiceHandler;
import org.sonatype.rest.impl.DefaultServiceDefinition;
import org.sonatype.rest.impl.JAXRSServiceDefinitionGenerator;
import org.sonatype.rest.impl.JAXRSServiceDefinitionProvider;

public class RestDeflectorServletModule extends ServletModule {

    @Override
    protected void configureServlets() {

        JAXRSServiceDefinitionGenerator g = new JAXRSServiceDefinitionGenerator(binder());
        bind(ServiceDefinitionGenerator.class).toInstance(g);
        
        bind(ServiceEntity.class).to(AddressBookServiceEntity.class);

        // First, bind our Service Provider
        bind(ServiceDefinition.class).toProvider(ServiceDefinitionProvider.class);
        bind(ServiceDefinitionProvider.class).to(JAXRSServiceDefinitionProvider.class);


// The code below works fine.
        
        ServiceDefinition serviceDefinition = new DefaultServiceDefinition(g);

        serviceDefinition.withPath("/service/{id}")
                .producing(ServiceDefinition.Media.JSON)
                .producing(ServiceDefinition.Media.XML)
                .consuming(ServiceDefinition.Media.JSON)
                .consuming(ServiceDefinition.Media.XML)
                        //.withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.PUT, "id", "createAddressBook"))
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.GET, "id", "getAddressBook"))
                .usingEntity(new AddressBookServiceEntity())
                .bind();


// The injection doesn't work. The generated class is never found by Jersey.
        //bind(ServiceDefinitionJAXRSTest.class).asEagerSingleton();


        serve("/*").with(GuiceContainer.class);

    }

}