package org.sonatype.server.resources;

import com.google.inject.Inject;
import org.sonatype.rest.ServiceDefinition;
import org.sonatype.rest.ServiceHandler;

public class ServiceDefinitionTest {

    @Inject
    ServiceDefinition serviceDefinition;

    public ServiceDefinitionTest() {
        defineService();
    }

    public void defineService() {

        serviceDefinition.withPath("/service/:type")
                .producing(ServiceDefinition.Media.JSON)
                .producing(ServiceDefinition.Media.XML)
                .consuming(ServiceDefinition.Media.JSON)
                .consuming(ServiceDefinition.Media.XML)
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.POST, "id", "test"))
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.GET, "id", "readPerson(id)"))
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.GET, "id", "readPeople()"))
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.PUT, "id", "updatePerson(entity)"))
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.DELETE, "id", "deletePerson(id)"))
                .bind();

    }

}
