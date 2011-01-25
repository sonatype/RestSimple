package org.sonatype.rest.impl;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.http.Delete;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Post;
import com.google.sitebricks.http.Put;
import com.google.sitebricks.http.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.rest.api.ServiceEntity;
import org.sonatype.rest.api.ServiceHandler;
import org.sonatype.rest.api.ServiceHandlerMediaType;
import org.sonatype.rest.spi.ServiceHandlerMapper;

import java.lang.reflect.Method;

@Select("Accept")
public final class SitebricksResource {

    private String update;

    private Logger logger = LoggerFactory.getLogger(SitebricksResource.class);

    @Inject
    ServiceEntity serviceEntity;

    @Inject
    ServiceHandlerMapper mapper;

    @Inject
    ServiceHandlerMediaType producer;

    @Get
    public Reply<?> get(@Named("method") String service, @Named("id") String value) {
        logger.debug("HTTP GET: Generated Resource invocation for method {} with id {}", service, value);
        Object response = createResponse("get", service, value, null);

        if (Reply.class.isAssignableFrom(response.getClass())) {
            return Reply.class.cast(response);
        }

        return Reply.with(producer.visit(response)).as(Json.class);
    }

    @Put
    public Reply<?> put(@Named("method") String service, @Named("id") String value) {
        logger.debug("HTTP PUT: Generated Resource invocation for method {} with id {}", service, value);
        //URI location = UriBuilder.fromResource(getClass()).build(new String[]{"", ""});
        Object response = createResponse("put", service, value, null);

        if (Reply.class.isAssignableFrom(response.getClass())) {
            return Reply.class.cast(response);
        }

        return Reply.with(response.toString()).status(201);
    }

    @Post
    public Reply<?> post(@Named("method") String service, @Named("id") String value) {
        logger.debug("HTTP POST: Generated Resource invocation for method {} with id {} and update {}", service, value);
        Object response = createResponse("post", service, value, update);

        if (Reply.class.isAssignableFrom(response.getClass())) {
            return Reply.class.cast(response);
        }

        if (response == null) {
            return Reply.with("").noContent();
        } else {
            return Reply.with(response.toString());
        }
    }

    @Delete
    public Reply<?> delete(@Named("method") String service, @Named("id") String value) {
        logger.debug("HTTP DELETE: Generated Resource invocation for method {} with id {}", service, value);
        Object response = createResponse("delete", service, value, null);
        return Reply.with(response.toString());
    }

    private Object createResponse(String methodName, String service, String value, String postUpdate) {
        ServiceHandler serviceHandler = mapper.map(service);
        if (serviceHandler == null) {
            return Reply.with("No ServiceHandler defined for service " + service).error();
        }

        if (!serviceHandler.getHttpMethod().name().equalsIgnoreCase(methodName)) {
            return Reply.with("Method not allowed").status(405);
        }

        String methodString = serviceHandler.getMethod();
        try {
            Method method = null;
            Object response;
            if (postUpdate != null) {
                method = serviceEntity.getClass().getMethod(methodString, new Class[]{String.class, String.class});
                response = (String) method.invoke(serviceEntity, new String[]{value, postUpdate});
            } else {
                method = serviceEntity.getClass().getMethod(methodString, new Class[]{String.class});
                response = method.invoke(serviceEntity, new String[]{value});
            }
            return response;
        } catch (Throwable e) {
            return Reply.with(e).error();
        }
    }

    public void setUpdate(String update) {
        this.update = update;
    }
    
}


