package org.sonatype.rest;

import java.util.List;

public interface ServiceDefinition {

    enum HttpMethod {
        POST, GET, PUT, DELETE
    }

    enum Media {
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
