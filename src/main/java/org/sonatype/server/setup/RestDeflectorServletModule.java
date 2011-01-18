package org.sonatype.server.setup;

import com.google.inject.servlet.ServletModule;
import org.sonatype.rest.DefaultServiceDefinition;
import org.sonatype.rest.ServiceDefinition;

public class RestDeflectorServletModule extends ServletModule {


    @Override

    protected void configureServlets() {
        bind(ServiceDefinition.class).to(DefaultServiceDefinition.class).asEagerSingleton();





    }

}