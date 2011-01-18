package org.sonatype.server.resources;

import com.google.inject.Inject;
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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;


@Path("/service/:type")
@Produces({APPLICATION_JSON, APPLICATION_XML})
@Consumes({APPLICATION_JSON, APPLICATION_XML})
public class ServiceDescriptionResource {

    @Inject
    ServiceEntity delegate;


    @POST
    public Response post(@PathParam("createPerson(entity)") ServiceHandler serviceHandler) {
        // Will properly invoke the "createPerson(entity)" as the ServiceHandler would have been appropriately generated
        Response response = createResponse(serviceHandler.delegatePost(delegate));
        return response;
    }

    @GET
    @Path("/:id")
    public Response get(@PathParam("readPerson(id)") ServiceHandler serviceHandler) {
        // Will properly invoke the "readPerson(id)" as the ServiceHandler would have been appropriately generated
        Response response = createResponse(serviceHandler.delegateGet(delegate));
        return response;
    }

    @GET
    public Response get0(@PathParam("readPeople()") ServiceHandler serviceHandler) {
        // Will properly invoke the "readPeople()" as the ServiceHandler would have been appropriately generated
        Response response = createResponse(serviceHandler.delegateGet(delegate));
        return response;
    }

    @PUT
    @Path("/:id")
    public Response put(@PathParam("updatePerson(entity)") ServiceHandler serviceHandler) {
        // Will properly invoke the "updatePerson(entity)" as the ServiceHandler would have been appropriately generated
        Response response = createResponse(serviceHandler.delegatePut(delegate));
        return response;
    }

    @DELETE
    @Path("/:id")    
    public Response delete(@PathParam("deletePerson(id)")ServiceHandler serviceHandler) {
        // Will properly invoke the "updatePerson(entity)" as the ServiceHandler would have been appropriately generated
        Response response = createResponse(serviceHandler.delegateDelete(delegate));
        return response;
    }

    private Response createResponse(ServiceHandler serviceHandler) {
        return Response.ok().build();
    }

}
