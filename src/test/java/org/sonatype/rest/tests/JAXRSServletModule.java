package org.sonatype.rest.tests;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.sonatype.rest.api.ServiceDefinition;
import org.sonatype.rest.api.ServiceEntity;
import org.sonatype.rest.api.ServiceHandler;
import org.sonatype.rest.api.ServiceHandlerMediaType;
import org.sonatype.rest.guice.JaxrsModule;

import java.util.HashMap;

public class JAXRSServletModule extends ServletModule {

    @Override
    protected void configureServlets() {

        ServiceEntity serviceEntity = new AddressBookServiceEntity();
        bind(ServiceHandlerMediaType.class).to(AddressBookMediaType.class);
        bind(ServiceEntity.class).toInstance(serviceEntity);
        
        Injector injector = Guice.createInjector(new JaxrsModule(binder().withSource("[generated]")));

        ServiceDefinition serviceDefinition = injector.getInstance(ServiceDefinition.class);
        serviceDefinition.withPath("/{method}/{id}")
                .producing(ServiceDefinition.Media.JSON)
                .producing(ServiceDefinition.Media.XML)
                .consuming(ServiceDefinition.Media.JSON)
                .consuming(ServiceDefinition.Media.XML)
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.PUT, "id", "createAddressBook"))
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.GET, "id", "getAddressBook"))
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.POST, "id", "updateAddressBook"))
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.DELETE, "id", "deleteAddressBook"))
                .usingEntity(serviceEntity)
                .bind();

        // TODO: This is NOT portable
        HashMap<String,String> initParams = new HashMap<String, String>();
        initParams.put("com.sun.jersey.api.json.POJOMappingFeature", "true");

        serve("/*").with(GuiceContainer.class, initParams);

    }

}

