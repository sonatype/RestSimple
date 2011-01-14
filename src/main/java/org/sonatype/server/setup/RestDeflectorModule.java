package org.sonatype.server.setup;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.util.Providers;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.sonatype.server.resources.ApplicationProxyResource;
import org.sonatype.server.resources.CachingJacksonJsonProvider;

import java.nio.charset.Charset;
import java.util.Random;

public class RestDeflectorModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Charset.class).toInstance(Charset.forName("UTF-8"));
        bind(Random.class).toInstance(new Random());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationConfig.Feature.WRITE_NULL_PROPERTIES, false);
        objectMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        JacksonJsonProvider jacksonJsonProvider = new CachingJacksonJsonProvider(objectMapper);
        bind(JacksonJsonProvider.class).toProvider(Providers.of(jacksonJsonProvider)).in(Singleton.class);

        bind(ApplicationProxyResource.class).asEagerSingleton();

    }
}