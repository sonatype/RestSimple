package org.sonatype.restsimple.example.addressBook;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.sonatype.restsimple.api.DeleteServiceHandler;
import org.sonatype.restsimple.api.GetServiceHandler;
import org.sonatype.restsimple.api.MediaType;
import org.sonatype.restsimple.api.PostServiceHandler;
import org.sonatype.restsimple.api.PutServiceHandler;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.api.ServiceEntity;
import org.sonatype.restsimple.jaxrs.guice.JaxrsModule;
import org.sonatype.restsimple.tests.AddressBookMediaType;
import org.sonatype.restsimple.tests.AddressBookServiceEntity;

public class AddressBookServletModule extends com.google.inject.servlet.ServletModule {

    @Override
    protected void configureServlets() {
        Injector injector = Guice.createInjector(new JaxrsModule(binder().withSource("[generated]")));

        ServiceEntity serviceEntity = new AddressBookServiceEntity();
        ServiceDefinition serviceDefinition = injector.getInstance(ServiceDefinition.class);

        PostServiceHandler postServiceHandler = new PostServiceHandler("id", "updateAddressBook");
        postServiceHandler.addFormParam("update");

        serviceDefinition
                .producing(new MediaType(AddressBookServiceEntity.APPLICATION, AddressBookServiceEntity.JSON))
                .producing(new MediaType(AddressBookServiceEntity.APPLICATION, AddressBookServiceEntity.XML))
                .consuming(MediaType.JSON)
                .consuming(MediaType.XML)
                .withHandler(new PutServiceHandler("id", "createAddressBook"))
                .withHandler(new GetServiceHandler("id", "getAddressBook", AddressBookMediaType.class))
                .withHandler(postServiceHandler)
                .withHandler(new DeleteServiceHandler("id", "deleteAddressBook"))
                .usingEntity(serviceEntity)
                .bind();


        postServiceHandler = new PostServiceHandler("id", "updateAddressBook");
        postServiceHandler.addFormParam("update");
        postServiceHandler.addFormParam("update2");

        serviceDefinition = injector.getInstance(ServiceDefinition.class);
        serviceDefinition
                .withPath("/foo")
                .producing(new MediaType(AddressBookServiceEntity.APPLICATION, AddressBookServiceEntity.JSON))
                .producing(new MediaType(AddressBookServiceEntity.APPLICATION, AddressBookServiceEntity.XML))
                .consuming(MediaType.JSON)
                .consuming(MediaType.XML)
                .withHandler(new PutServiceHandler("id", "createAddressBook"))
                .withHandler(new GetServiceHandler("id", "getAddressBook", AddressBookMediaType.class))
                .withHandler(postServiceHandler)
                .withHandler(new DeleteServiceHandler("id", "deleteAddressBook"))
                .usingEntity(serviceEntity)
                .bind();

        postServiceHandler = new PostServiceHandler("id", "updateAddressBook");

        serviceDefinition = injector.getInstance(ServiceDefinition.class);
        serviceDefinition
                .withPath("/bar")
                .producing(new MediaType(AddressBookServiceEntity.APPLICATION, AddressBookServiceEntity.JSON))
                .producing(new MediaType(AddressBookServiceEntity.APPLICATION, AddressBookServiceEntity.XML))
                .consuming(MediaType.JSON)
                .consuming(MediaType.XML)
                .withHandler(new PutServiceHandler("id", "createAddressBook"))
                .withHandler(new GetServiceHandler("id", "getAddressBook", AddressBookMediaType.class))
                .withHandler(postServiceHandler)
                .withHandler(new DeleteServiceHandler("id", "deleteAddressBook"))
                .usingEntity(serviceEntity)
                .bind();
        
        serve("/*").with(GuiceContainer.class);

    }

}

