package org.sonatype.server.setup;

import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.sonatype.rest.impl.JAXRSServiceDefinitionGenerator;
import org.sonatype.rest.impl.JAXRSServiceDefinitionProvider;
import org.sonatype.rest.api.ServiceDefinition;
import org.sonatype.rest.api.ServiceDefinitionGenerator;
import org.sonatype.rest.api.ServiceDefinitionProvider;
import org.sonatype.rest.api.ServiceEntity;
import org.sonatype.server.resources.ServiceDefinitionTest;

import java.util.HashMap;
import java.util.Map;

public class RestDeflectorServletModule extends ServletModule {

    private Map<String, String> initParams = new HashMap<String, String>();

    @Override
    protected void configureServlets() {

        bind(ServiceEntity.class).to(AddressBookServiceEntity.class);

        // First, bind our Service Provider
        bind(ServiceDefinitionProvider.class).to(JAXRSServiceDefinitionProvider.class);

        // Next, Define our Resource Generator
        bind(ServiceDefinitionGenerator.class).toInstance(new JAXRSServiceDefinitionGenerator(binder()));

        // Finally bind our Provider
        bind(ServiceDefinition.class).toProvider(JAXRSServiceDefinitionProvider.class);

        //Finally, bind our implementation
        bind(ServiceDefinitionTest.class).asEagerSingleton();


        initParams.put("com.sun.jersey.config.property.packages", "org.sonatype.server.resources");
        serve("/*").with(GuiceContainer.class, initParams);
        
    }

}