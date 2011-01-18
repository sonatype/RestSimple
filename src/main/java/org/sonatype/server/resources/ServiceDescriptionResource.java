package org.sonatype.server.resources;

import com.google.inject.Inject;
import org.sonatype.rest.ServiceDefinition;
import org.sonatype.rest.ServiceEntity;
import org.sonatype.rest.ServiceHandler;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import java.lang.reflect.Method;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

/**
 * This is what we want to generate automatically using ASM.
 *
 * TODO: Not sure @PathParam use is portable.
 * 
 */
@Path("/service/")
@Produces({APPLICATION_JSON, APPLICATION_XML})
@Consumes({APPLICATION_JSON, APPLICATION_XML})
public class ServiceDescriptionResource {

    @Inject
    ServiceEntity delegate;

    @Inject
    ServiceDefinition serviceDefinition;

    @POST
    @Path("{id}")    
    public Response put(@PathParam("createPerson(id)") ServiceHandler serviceHandler, @PathParam("{id}") String entity) {
        // Will properly invoke the "createPerson(id)" as the ServiceHandler would have been appropriately generated
        Response response = createResponse(serviceHandler, entity);
        return response;
    }

    @GET
    @Path("{id}")
    public Response get(@PathParam("readPerson(id)") ServiceHandler serviceHandler, @PathParam("{id}") String entity) {
        // Will properly invoke the "readPerson(id)" as the ServiceHandler would have been appropriately generated
        Response response = createResponse(serviceHandler, entity);;
        return response;
    }

    @GET
    public Response get0(@PathParam("readPersons()") ServiceHandler serviceHandler) {
        // Will properly invoke the "readPeople()" as the ServiceHandler would have been appropriately generated
        Response response = createResponse(serviceHandler, null);
        return response;
    }

    @PUT
    @Path("{id}")
    public Response post(@PathParam("updatePerson(id)") ServiceHandler serviceHandler, @PathParam("{id}") String entity) {
        // Will properly invoke the "updatePerson(id)" as the ServiceHandler would have been appropriately generated
        Response response = createResponse(serviceHandler, entity);
        return response;
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("deletePerson(id)") ServiceHandler serviceHandler, String entity) {
        // Will properly invoke the "updatePerson(entity)" as the ServiceHandler would have been appropriately generated
        Response response = createResponse(serviceHandler, entity);
        return response;
    }

    private Response createResponse(ServiceHandler serviceHandler, String value) {
        // OK this is where the fun begin
        String methodString = serviceHandler.getMethod();
        try {
            Method method = delegate.getClass().getMethod(methodString, new Class[]{String.class});
            return Response.ok(method.invoke(new Object[]{value})).build();
        } catch (Throwable e) {
            // TODO: log me.
            return Response.serverError().build();
        }
    }

}
