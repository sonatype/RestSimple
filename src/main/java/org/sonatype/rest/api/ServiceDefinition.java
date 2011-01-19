package org.sonatype.rest.api;

import java.util.List;

public interface ServiceDefinition {

    public enum HttpMethod {
        POST, GET, PUT, DELETE
    }

    public enum Media {
        JSON, XML
    }

    ServiceDefinition usingEntity(ServiceEntity d);

    ServiceDefinition withPath(String s);

    ServiceDefinition withHandler(ServiceHandler mapping);

    ServiceDefinition producing(Media media);

    ServiceDefinition consuming(Media media);

    String basePath();

    ServiceEntity serviceEntity();

    List<ServiceHandler> serviceHandlers();

    List<Media> mediaToConsume();

    List<Media> mediaToProduce();

    void bind();
}
