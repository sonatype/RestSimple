package org.sonatype.server.setup;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.sonatype.rest.api.ServiceDefinition;
import org.sonatype.rest.api.ServiceEntity;
import org.sonatype.rest.api.ServiceHandler;

public class RestDeflectorServletModule extends ServletModule {

    @Override
    protected void configureServlets() {

        Injector injector = Guice.createInjector(new JaxrsModule(binder().withSource("[generated]")));

        ServiceDefinition serviceDefinition = injector.getInstance(ServiceDefinition.class);

        serviceDefinition.withPath("/service/{id}")
                .producing(ServiceDefinition.Media.JSON)
                .producing(ServiceDefinition.Media.XML)
                .consuming(ServiceDefinition.Media.JSON)
                .consuming(ServiceDefinition.Media.XML)
                        //.withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.PUT, "id", "createAddressBook"))
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.GET, "id", "getAddressBook"))
                .usingEntity(injector.getInstance(ServiceEntity.class))
                .bind();

        serve("/*").with(GuiceContainer.class);

    }

}