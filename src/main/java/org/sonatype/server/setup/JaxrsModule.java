package org.sonatype.server.setup;

import com.google.inject.AbstractModule;
import org.sonatype.rest.api.ServiceDefinition;
import org.sonatype.rest.api.ServiceHandler;
import org.sonatype.rest.impl.DefaultServiceDefinition;
import org.sonatype.rest.impl.JAXRSServiceDefinitionGenerator;

public class JaxrsModule extends AbstractModule {


    @Override
    protected void configure() {

        JAXRSServiceDefinitionGenerator g = new JAXRSServiceDefinitionGenerator(binder());

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

    }
}
