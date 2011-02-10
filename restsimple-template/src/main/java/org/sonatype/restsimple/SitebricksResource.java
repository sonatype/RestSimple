package org.sonatype.restsimple;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.sitebricks.At;
import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.client.transport.Text;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.http.Delete;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Post;
import com.google.sitebricks.http.Put;
import com.google.sitebricks.http.negotiate.Accept;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.restsimple.api.PostServiceHandler;
import org.sonatype.restsimple.api.ServiceEntity;
import org.sonatype.restsimple.api.ServiceHandler;
import org.sonatype.restsimple.api.ServiceHandlerMediaType;
import org.sonatype.restsimple.spi.ServiceHandlerMapper;

import java.lang.reflect.Method;
import java.util.List;

/**
 * This is what we want to generate automatically using ASM.
 * <p/>
 * NOTE: This class is not used, but we do instead generate some form ot it based on {@link org.sonatype.restsimple.api.ServiceDefinition}
 */
@At("/:method/:id")
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
    @Accept("application/vnd.org.sonatype.rest+json")
    public Reply<?> get(@Named("method") String service, @Named("id") String value) {
        logger.debug("HTTP GET: Generated Resource invocation for method {} with id {}", service, value);
        Object response = createResponse("get", service, value);

        if (Reply.class.isAssignableFrom(response.getClass())) {
            return Reply.class.cast(response);
        }

        return Reply.with(producer.visit(response)).as(Json.class);
    }

    @Put
    @Accept("application/vnd.org.sonatype.rest+json")
    public Reply<?> put(@Named("method") String service, @Named("id") String value) {
        logger.debug("HTTP PUT: Generated Resource invocation for method {} with id {}", service, value);
        Object response = createResponse("put", service, value);

        if (Reply.class.isAssignableFrom(response.getClass())) {
            return Reply.class.cast(response);
        }

        return Reply.with(response.toString()).status(201);
    }

    @Post @Accept("application/vnd.org.sonatype.rest+json")
    public Reply<?> post0(@Named("method") String service, @Named("id") String value) {
        logger.debug("HTTP POST: Generated Resource invocation for method {} with id {} and update {}", service, value);
        Object response = createResponse("post", service, value);

        if (Reply.class.isAssignableFrom(response.getClass())) {
            return Reply.class.cast(response);
        }

        if (response == null) {
            return Reply.with("").noContent();
        } else {
            return Reply.with(response.toString());
        }
    }

    @Post
    @Accept("application/vnd.org.sonatype.rest+json")
    public Reply<?> post(@Named("method") String service, @Named("id") String value, Request request) {
        logger.debug("HTTP POST: Generated Resource invocation for method {} with id {} and update {}", service, value);
        String body = request.read(String.class).as(Text.class);
        Object response = createResponse("post", service, value, body);

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
    @Accept("application/vnd.org.sonatype.rest+json")
    public Reply<?> delete(@Named("method") String service, @Named("id") String value) {
        logger.debug("HTTP DELETE: Generated Resource invocation for method {} with id {}", service, value);
        Object response = createResponse("delete", service, value);
        return Reply.with(response.toString());
    }

    private Object createResponse(String methodName, String service, String value) {
        ServiceHandler serviceHandler = mapper.map(service);
        if (serviceHandler == null) {
            return Reply.with("No ServiceHandler defined for service " + service).error();
        }

        if (!serviceHandler.getHttpMethod().name().equalsIgnoreCase(methodName)) {
            return Reply.with("Method not allowed").status(405);
        }

        String methodString = serviceHandler.getServiceEntityMethod();
        Class[] classes = new Class[1];
        classes[0] = String.class;
        Object[] objects = new Object[1];
        objects[0] = value;
        try {
            Object response;
            if (PostServiceHandler.class.isAssignableFrom(serviceHandler.getClass())) {
                List<String> formParams = PostServiceHandler.class.cast(serviceHandler).formParams();
                classes = new Class[formParams.size() + 1];
                objects = new Object[formParams.size() + 1];

                classes[0] = String.class;
                objects[0] = value;

                int i = 1;
                for (String formParam : formParams) {
                    classes[i] = String.class;
                    objects[i++] = (String) this.getClass().getDeclaredField(formParam).get(this);
                    logger.info("Getting generated value for form param {} : {}", formParam, this.getClass().getDeclaredField(formParam).get(this));
                }
            }
            Method method = serviceEntity.getClass().getMethod(methodString, classes);
            response = method.invoke(serviceEntity, objects);

            return response;
        } catch (Throwable e) {
            logger.error("createResponse", e);
            return Reply.with(e).error();
        }
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    private Object createResponse(String methodName, String service, String value, String body) {
        ServiceHandler serviceHandler = mapper.map(service);
        if (serviceHandler == null) {
            return Reply.with("No ServiceHandler defined for service " + service).error();
        }

        if (!serviceHandler.getHttpMethod().name().equalsIgnoreCase(methodName)) {
            return Reply.with("Method not allowed").status(405);
        }

        String methodString = serviceHandler.getServiceEntityMethod();
        Class[] classes = new Class[2];
        classes[0] = String.class;
        classes[1] = String.class;
        Object[] objects = new Object[2];
        objects[0] = value;
        objects[1] = body;
        try {
            Method method = serviceEntity.getClass().getMethod(methodString, classes);
            return method.invoke(serviceEntity, objects);
        } catch (Throwable e) {
            logger.error("delegate", e);
            return Reply.with(e).error();
        }
    }
}


