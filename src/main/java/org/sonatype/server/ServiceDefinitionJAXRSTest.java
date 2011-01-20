package org.sonatype.server;

import com.google.inject.Inject;
import org.sonatype.rest.api.ServiceDefinition;
import org.sonatype.rest.api.ServiceEntity;
import org.sonatype.rest.api.ServiceHandler;

public class ServiceDefinitionJAXRSTest {

    @Inject
    public ServiceDefinitionJAXRSTest(ServiceDefinition serviceDefinition, ServiceEntity entity) {
        defineService(serviceDefinition, entity);
    }

    public void defineService(ServiceDefinition serviceDefinition, ServiceEntity entity) {

        serviceDefinition.withPath("/service/{id}")
                .producing(ServiceDefinition.Media.JSON)
                .producing(ServiceDefinition.Media.XML)
                .consuming(ServiceDefinition.Media.JSON)
                .consuming(ServiceDefinition.Media.XML)
                //.withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.PUT, "id", "createAddressBook"))
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.GET, "id", "getAddressBook"))
                .usingEntity(entity)
                .bind();

    }

}