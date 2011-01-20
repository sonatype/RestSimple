package org.sonatype.rest.impl;

import com.google.inject.Binder;
import org.sonatype.rest.api.ServiceDefinition;
import org.sonatype.rest.api.ServiceDefinitionGenerator;
import org.sonatype.rest.api.ServiceEntity;
import org.sonatype.rest.api.ServiceHandler;
import org.sonatype.rest.api.ServiceHandlerMapper;

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

    //
    // This allows a particular method to be selected based on a parameter in the request
    //
    // @Select("metadata")
    // @Post
    // @Get
    // @At( "/:id" ) @Get
    // @At( "/:id" ) @Put
    // @At( "/:id" ) @Delete
    // @Get("form")

    protected DefaultServiceDefinition(ServiceDefinitionGenerator generator, ServiceHandlerMapper serviceHandlerMapper) {
        this.generator = generator;
        this.serviceHandlerMapper = serviceHandlerMapper;
    }

    @Override    
    public ServiceDefinition withPath(String basePath) {
        this.basePath = basePath;
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
    public String basePath() {
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
