package org.sonatype.rest.impl;

import org.sonatype.rest.api.ServiceDefinition;
import org.sonatype.rest.spi.ServiceDefinitionGenerator;
import org.sonatype.rest.api.ServiceEntity;
import org.sonatype.rest.api.ServiceHandler;
import org.sonatype.rest.spi.ServiceHandlerMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultServiceDefinition implements ServiceDefinition {
    private String basePath;
    private ServiceEntity serviceEntity;
    private final List<Media> mediaToProduce = new ArrayList<Media>();
    private final List<Media> mediaToConsume = new ArrayList<Media>();
    private final List<ServiceHandler> serviceHandlers = new ArrayList<ServiceHandler>();
    private final ServiceDefinitionGenerator generator;
    private final ServiceHandlerMapper serviceHandlerMapper;

    protected DefaultServiceDefinition(ServiceDefinitionGenerator generator, ServiceHandlerMapper serviceHandlerMapper) {
        this.generator = generator;
        this.serviceHandlerMapper = serviceHandlerMapper;
    }

    @Override    
    public ServiceDefinition withPath(String path) {
        this.basePath = path;
        return this;
    }

    @Override
    public ServiceDefinition usingEntity(ServiceEntity serviceEntity) {
        this.serviceEntity = serviceEntity;
        return this;
    }

    @Override
    public ServiceDefinition withHandler(ServiceHandler serviceHandler) {
        serviceHandlers.add(serviceHandler);
        serviceHandlerMapper.addServiceHandler(serviceHandler);
        return this;
    }

    @Override
    public ServiceDefinition producing(Media media) {
        mediaToProduce.add(media);
        return this;
    }

    @Override
    public ServiceDefinition consuming(Media media) {
        mediaToConsume.add(media);
        return this;
    }

    @Override
    public String path() {
        return basePath;
    }

    @Override
    public ServiceEntity serviceEntity() {
        return serviceEntity;
    }

    @Override
    public List<ServiceHandler> serviceHandlers() {
        return Collections.unmodifiableList(serviceHandlers);
    }

    @Override
    public List<Media> mediaToConsume() {
        return Collections.unmodifiableList(mediaToConsume);
    }

    @Override
    public List<Media> mediaToProduce() {
        return Collections.unmodifiableList(mediaToProduce);
    }

    @Override
    public void bind() {
        if (basePath == null) {
            throw new NullPointerException("withPath must be invoked with a non null value");
        }

        if (serviceEntity == null) {
            throw new NullPointerException("usingEntity must be invoked with a non null value");
        }

        generator.generate(this);

    }
}
