package org.sonatype.rest.sitebricks;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.ServletModule;
import org.sonatype.rest.api.MediaType;
import org.sonatype.rest.api.ServiceDefinition;
import org.sonatype.rest.api.ServiceEntity;
import org.sonatype.rest.api.ServiceHandler;
import org.sonatype.rest.api.ServiceHandlerMediaType;
import org.sonatype.rest.guice.SitebricksModule;

public class SitebricksServletModule extends ServletModule {

    @Override
    protected void configureServlets() {

        ServiceEntity serviceEntity = new AddressBookServiceEntity();
        bind(ServiceEntity.class).toInstance(serviceEntity);
        
        Injector injector = Guice.createInjector(new SitebricksModule(binder()));

        ServiceDefinition serviceDefinition = injector.getInstance(ServiceDefinition.class);
        serviceDefinition
                .producing(new MediaType(AddressBookServiceEntity.APPLICATION, AddressBookServiceEntity.JSON))
                .producing(new MediaType(AddressBookServiceEntity.APPLICATION, AddressBookServiceEntity.XML))
                .consuming(MediaType.JSON)
                .consuming(MediaType.XML)
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.PUT, "id", "createAddressBook"))
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.GET, "id", "getAddressBook", AddressBookMediaType.class))
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.POST, "id", "updateAddressBook"))
                .withHandler(new ServiceHandler(ServiceDefinition.HttpMethod.DELETE, "id", "deleteAddressBook"))
                .usingEntity(serviceEntity)
                .bind();

    }

}

