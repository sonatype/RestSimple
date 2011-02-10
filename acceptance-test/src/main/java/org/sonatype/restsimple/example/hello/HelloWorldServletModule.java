package org.sonatype.restsimple.example.hello;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.sonatype.restsimple.api.GetServiceHandler;
import org.sonatype.restsimple.api.MediaType;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.api.ServiceEntity;
import org.sonatype.restsimple.jaxrs.guice.JaxrsModule;

public class HelloWorldServletModule extends com.google.inject.servlet.ServletModule {

    @Override
    protected void configureServlets() {
        Injector injector = Guice.createInjector(new JaxrsModule(binder().withSource("[generated]")));

        ServiceEntity serviceEntity = new HelloWorldServiceEntity();
        ServiceDefinition serviceDefinition = injector.getInstance(ServiceDefinition.class);
        serviceDefinition
                .withHandler(new GetServiceHandler("name", "sayPlainTextHello", HelloWorldMediaType.class).producing(
                        new MediaType(HelloWorldServiceEntity.APPLICATION, HelloWorldServiceEntity.TXT)))
                .withHandler(new GetServiceHandler("name", "sayPlainXmlHello", HelloWorldMediaType.class).producing(
                        new MediaType(HelloWorldServiceEntity.APPLICATION, HelloWorldServiceEntity.XML)))
                .withHandler(new GetServiceHandler("name", "sayPlainHtmlHello", HelloWorldMediaType.class).producing(
                        new MediaType(HelloWorldServiceEntity.APPLICATION, HelloWorldServiceEntity.HTML)))
                .usingEntity(serviceEntity)
                .bind();

        serve("/*").with(GuiceContainer.class);

    }

}

