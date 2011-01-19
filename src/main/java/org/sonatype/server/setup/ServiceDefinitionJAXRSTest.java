package org.sonatype.server.setup;

import com.google.inject.Inject;
import org.sonatype.rest.api.ServiceDefinition;
import org.sonatype.rest.api.ServiceHandler;

public class ServiceDefinitionJAXRSTest {

    @Inject
    ServiceDefinition serviceDefinition;

    public ServiceDefinitionJAXRSTest() {
        defineService();
    }

    public void defineService() {

        serviceDefinition.withPath("/service/{id}")
                .producing(ServiceDefinition.Media.JSON)
                .producing(ServiceDefinition.Media.XML)
                .consuming(ServiceDefinition.Media.JSON)
                .consuming(ServiceDefinition.Media.XML)
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.POST, "id", "createAddressBook"))
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.GET, "id", "getAddressBook"))
                .bind();

    }

}