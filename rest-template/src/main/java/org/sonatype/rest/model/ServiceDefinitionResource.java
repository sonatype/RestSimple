package org.sonatype.rest.model;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.rest.api.PostServiceHandler;
import org.sonatype.rest.api.ServiceEntity;
import org.sonatype.rest.api.ServiceHandler;
import org.sonatype.rest.api.ServiceHandlerMediaType;
import org.sonatype.rest.spi.ServiceHandlerMapper;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * This is what we want to generate automatically using ASM.
 * <p/>
 * NOTE: This class is not used, but we do instead generate some form ot it based on {@link org.sonatype.rest.api.ServiceDefinition}
 */
@Path("/bar/{method}/{id}/")
@Produces("application/vnd.org.sonatype.rest+json")
@Consumes("application/vnd.org.sonatype.rest+json")
public class ServiceDefinitionResource {

    private Logger logger = LoggerFactory.getLogger(ServiceDefinitionResource.class);

    @Inject
    ServiceEntity serviceEntity;

    @Inject
    ServiceHandlerMapper mapper;

    @Inject
    ServiceHandlerMediaType producer;

    @GET
    public ServiceHandlerMediaType get(@PathParam("method") String service, @PathParam("id") String value) {
        logger.debug("HTTP GET: Generated Resource invocation for method {} with id {}", service, value);
        Object response = createResponse("get", service, value, null);
        return producer.visit(response);
    }

    @HEAD
    public Response head(@PathParam("method") String service, @PathParam("id") String value) {
        logger.debug("HTTP HEAD: Generated Resource invocation for method {} with id {}", service, value);
        Object response = createResponse("head", service, value, null);
        return Response.ok().build();
    }

    @PUT
    public Response put(@PathParam("method") String service, @PathParam("id") String value) {
        logger.debug("HTTP PUT: Generated Resource invocation for method {} with id {}", service, value);
        URI location = UriBuilder.fromResource(getClass()).build(new String[]{"", ""});
        Object response = createResponse("put", service, value, null);
        return Response.created(location).entity(response).build();
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    public Response post(@PathParam("method") String service, @PathParam("id") String value, MultivaluedMap<String, String> formParams ) {
        logger.debug("HTTP POST: Generated Resource invocation for method {} with id {} and update {}", service, value);
        Object response = createResponse("post", service, value, formParams);
        if (response == null) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.ok(response).build();
        }
    }
    
    @POST
    public Response postJson(@PathParam("method") String service, @PathParam("id") String value, String body) {
        logger.debug("HTTP POST: Generated Resource invocation for method {} with id {} and update {}", service, value);
        Object response = createResource("post", service, value, body);
        if (response == null) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.ok(response).build();
        }
    }

    @DELETE
    public Response delete(@PathParam("method") String service, @PathParam("id") String value) {
        logger.debug("HTTP DELETE: Generated Resource invocation for method {} with id {}", service, value);
        Object response = createResponse("delete", service, value, null);
        return Response.ok(response).build();
    }

    private Object createResponse(String methodName, String service, String value, MultivaluedMap<String, String> formParams) {
        ServiceHandler serviceHandler = mapper.map(service);
        if (serviceHandler == null) {
            throw new WebApplicationException(new IllegalStateException("No ServiceHandler defined for service " + service));
        }

        if (!serviceHandler.getHttpMethod().name().equalsIgnoreCase(methodName)) {
            throw new WebApplicationException(Response.status(405).entity("Method not allowed").build());
        }

        String methodString = serviceHandler.getServiceEntityMethod();
        Class[] classes = new Class[1];
        classes[0] = String.class;
        Object[] objects = new Object[1];
        objects[0] = value;
        try {
            Object response;
            if (PostServiceHandler.class.isAssignableFrom(serviceHandler.getClass())) {

                if (formParams == null) {
                    throw new WebApplicationException(Response.status(500).entity("No form params").build());
                }
                                
                classes = new Class[formParams.size() + 1];
                objects = new Object[formParams.size() + 1];

                classes[0] = String.class;
                objects[0] = value;

                int i = 1;
                List<String> values = null;
                for(Map.Entry<String, List<String>> entry: formParams.entrySet()) {
                    values = entry.getValue();
                    classes[i] = String.class;
                    objects[i++] = values.get(0);
                }
            }
            Method method = serviceEntity.getClass().getMethod(methodString, classes);
            response = method.invoke(serviceEntity, objects);

            return response;
        } catch (Throwable e) {
            logger.error("createResponse", e);
            throw new WebApplicationException(e);
        }
    }

    private Object createResource(String methodName, String service, String value, String body) {
        ServiceHandler serviceHandler = mapper.map(service);
        if (serviceHandler == null) {
            throw new WebApplicationException(new IllegalStateException("No ServiceHandler defined for service " + service));
        }

        if (!serviceHandler.getHttpMethod().name().equalsIgnoreCase(methodName)) {
            throw new WebApplicationException(Response.status(405).entity("Method not allowed").build());
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
            logger.error("createResponse", e);
            throw new WebApplicationException(e);
        }
    }
}
