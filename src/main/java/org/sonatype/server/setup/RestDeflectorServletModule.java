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

        bind(ServiceEntity.class).to(AddressBookServiceEntity.class);

        // First, bind our Service Provider
        bind(ServiceDefinitionProvider.class).to(JAXRSServiceDefinitionProvider.class);

        bind(ServiceDefinition.class).toProvider(ServiceDefinitionProvider.class);

        JAXRSServiceDefinitionGenerator g = new JAXRSServiceDefinitionGenerator(binder());
        // Next, Define our Resource Generator
        bind(ServiceDefinitionGenerator.class).toInstance(g);

//        ServiceDefinition serviceDefinition = new DefaultServiceDefinition(g);
//
//        serviceDefinition.withPath("/service/{id}")
//                .producing(ServiceDefinition.Media.JSON)
//                .producing(ServiceDefinition.Media.XML)
//                .consuming(ServiceDefinition.Media.JSON)
//                .consuming(ServiceDefinition.Media.XML)
//                        //.withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.PUT, "id", "createAddressBook"))
//                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.GET, "id", "getAddressBook"))
//                .usingEntity(new AddressBookServiceEntity())
//                .bind();

        bind(ServiceDefinitionJAXRSTest.class);


        serve("/*").with(GuiceContainer.class);

    }

}