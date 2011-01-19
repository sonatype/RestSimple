package org.sonatype.server.setup;

import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.sonatype.rest.impl.JAXRSServiceDefinitionGenerator;
import org.sonatype.rest.impl.JAXRSServiceDefinitionProvider;
import org.sonatype.rest.api.ServiceDefinition;
import org.sonatype.rest.api.ServiceDefinitionGenerator;
import org.sonatype.rest.api.ServiceDefinitionProvider;
import org.sonatype.rest.api.ServiceEntity;

import javax.ws.rs.Path;
import java.util.HashMap;
import java.util.Map;

public class RestDeflectorServletModule extends ServletModule {

    @Override
    protected void configureServlets() {

        bind(ServiceEntity.class).to(AddressBookServiceEntity.class);

        // First, bind our Service Provider
        bind(ServiceDefinitionProvider.class).to(JAXRSServiceDefinitionProvider.class);

        bind(ServiceDefinition.class).toProvider(JAXRSServiceDefinitionProvider.class);

        // Next, Define our Resource Generator
        bind(ServiceDefinitionGenerator.class).toInstance(new JAXRSServiceDefinitionGenerator(binder(), Path.class.getClassLoader()));

        //Finally, bind our implementation
        bind(ServiceDefinitionJAXRSTest.class).asEagerSingleton();

        serve("/*").with(GuiceContainer.class);
        
    }

}