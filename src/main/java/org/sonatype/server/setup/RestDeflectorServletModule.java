package org.sonatype.server.setup;

import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.container.filter.GZIPContentEncodingFilter;
import com.sun.jersey.api.core.ResourceConfig;
import org.atmosphere.guice.AtmosphereGuiceServlet;
import org.sonatype.etag.ETag;
import org.sonatype.etag.MD5Etag;
import org.sonatype.server.resources.ApplicationProxyResource;
import org.sonatype.server.store.ApplicationStore;
import org.sonatype.server.store.FileApplicationStore;
import org.sonatype.server.store.FileMessageStoreConfig;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.google.common.collect.ImmutableMap.of;

public class RestDeflectorServletModule extends ServletModule {


    @Override
    protected void configureServlets() {

        Names.bindProperties(binder(), System.getProperties());

        bind(ScheduledExecutorService.class).toInstance(Executors.newScheduledThreadPool(1));
        bind(FileMessageStoreConfig.class);
        bind(FileApplicationStore.class).asEagerSingleton();
        bind(ApplicationStore.class).to(FileApplicationStore.class).in(Singleton.class);
        bind(ETag.class).to(MD5Etag.class).in(Singleton.class);
        install(new RestDeflectorModule());

        bind(new TypeLiteral<Map<String, String>>() {
        }).annotatedWith(Names.named(AtmosphereGuiceServlet.JERSEY_PROPERTIES)).toInstance(
                of(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, GZIPContentEncodingFilter.class.getName()));
    }

}