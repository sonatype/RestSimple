package org.sonatype.rest.jaxrs;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.sonatype.rest.api.DeleteServiceHandler;
import org.sonatype.rest.api.GetServiceHandler;
import org.sonatype.rest.api.MediaType;
import org.sonatype.rest.api.PostServiceHandler;
import org.sonatype.rest.api.PutServiceHandler;
import org.sonatype.rest.api.ServiceDefinition;
import org.sonatype.rest.api.ServiceEntity;
import org.sonatype.rest.api.ServiceHandler;
import org.sonatype.rest.guice.JaxrsModule;

import java.util.HashMap;

public class JAXRSServletModule extends ServletModule {

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

        // TODO: This is NOT portable
        HashMap<String,String> initParams = new HashMap<String, String>();
        initParams.put("com.sun.jersey.api.json.POJOMappingFeature", "true");

        serve("/*").with(GuiceContainer.class, initParams);

    }

}

