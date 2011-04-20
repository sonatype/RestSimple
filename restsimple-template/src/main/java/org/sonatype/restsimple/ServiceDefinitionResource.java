/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.restsimple;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.restsimple.api.Action;
import org.sonatype.restsimple.api.ActionContext;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.api.ServiceHandler;
import org.sonatype.restsimple.spi.ServiceHandlerMapper;

import javax.servlet.http.HttpServletRequest;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is what we want to generate automatically using ASM.
 * <p/>
 * NOTE: This class is not used, but we do instead generate some form ot it based on {@link org.sonatype.restsimple.api.ServiceDefinition}
 */
@Path("/{method}/{id}/")
@Produces("application/vnd.org.sonatype.rest+json")
@Consumes("application/vnd.org.sonatype.rest+json")
public class ServiceDefinitionResource {

    private Logger logger = LoggerFactory.getLogger(ServiceDefinitionResource.class);

    @Context
    HttpServletRequest request;

    @Inject
    ServiceHandlerMapper mapper;

    @Path("something")
    @GET
    @Consumes("application/vnd.org.sonatype.rest+json")
    @Produces("application/vnd.org.sonatype.rest+json")
    public Object get(@Context UriInfo uriInfo) {
        Object response = invokeAction("get", uriInfo, null, mapMatrixParam(uriInfo), null);
        return response;
    }

    @Path("something")
    @HEAD
    @Consumes("application/vnd.org.sonatype.rest+json")
    @Produces("application/vnd.org.sonatype.rest+json")
    public Response head(@Context UriInfo uriInfo) {
        Object response = invokeAction("head", uriInfo, null, mapMatrixParam(uriInfo), null);
        return Response.ok().build();
    }

    @Path("something")
    @PUT
    @Consumes("application/vnd.org.sonatype.rest+json")
    @Produces("application/vnd.org.sonatype.rest+json")
    public Response put(@Context UriInfo uriInfo, Object jacksonObject) {
        URI location = UriBuilder.fromResource(getClass()).build(new String[]{"", "", ""});
        Object response = invokeAction("put", uriInfo, uriInfo.getQueryParameters(), mapMatrixParam(uriInfo), jacksonObject);
        return Response.created(location).entity(response).build();
    }

    @Path("something")
    @POST
    @Consumes("application/x-www-form-urlencoded")
    public Response post(@Context UriInfo uriInfo, MultivaluedMap<String, String> formParams) {
        Object response = invokeAction("post", uriInfo, formParams, mapMatrixParam(uriInfo), null);
        if (response == null) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.ok(response).build();
        }
    }

    @Path("something")
    @POST
    @Consumes("application/vnd.org.sonatype.rest+json")
    @Produces("application/vnd.org.sonatype.rest+json")
    public Response postWithBody(@Context UriInfo uriInfo, Object jacksonObject) {
        Object response = invokeAction("post", uriInfo, uriInfo.getQueryParameters(), mapMatrixParam(uriInfo), jacksonObject);
        if (response == null) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.ok(response).build();
        }
    }

    @Path("something")    
    @DELETE
    @Consumes("application/vnd.org.sonatype.rest+json")
    @Produces("application/vnd.org.sonatype.rest+json")
    public Response delete(@Context UriInfo uriInfo, Object jacksonObject) {
        Object response = invokeAction("delete", uriInfo, null, mapMatrixParam(uriInfo), jacksonObject);
        return Response.ok(response).build();
    }

    private <T> Object invokeAction(String methodName,
                                    UriInfo uriInfo,
                                    MultivaluedMap<String, String> formParams,
                                    Map<String, Collection<String>> matrixParams,
                                    T body) {

        ServiceHandler serviceHandler = mapper.map(methodName, uriInfo.getPath());
        if (serviceHandler == null) {
            throw new WebApplicationException(Response.status(405).entity("Method not allowed").build());
        }

        if (!serviceHandler.getHttpMethod().name().equalsIgnoreCase(methodName)) {
            throw new WebApplicationException(Response.status(405).entity("Method not allowed").build());
        }

        Object response = null;
        Action action = serviceHandler.getAction();


        String pathName = "";
        String pathValue = "";

        // TODO: Add support for multiple pathParam support.
        for (Map.Entry<String,List<String>> l : uriInfo.getPathParameters().entrySet()) {
            pathName = l.getKey();
            pathValue = l.getValue().get(0);
        }

        try {
            ActionContext<T> actionContext = new ActionContext<T>(mapMethod(request.getMethod()), mapHeaders(),
                    mapFormParams(formParams), matrixParams, request.getInputStream(), pathName, pathValue, body);
            response = action.action(actionContext);
        } catch (Throwable e) {
            logger.error("invokeAction", e);
            throw new WebApplicationException(e);
        }
        return response;
    }

    private Map<String, Collection<String>> mapFormParams(MultivaluedMap<String, String> formParams) {
        Map<String, Collection<String>> map = new HashMap<String, Collection<String>>();
        if (formParams != null) {
            for (String hn : formParams.keySet()) {
                List<String> l;
                if (map.get(hn) != null) {
                    map.get(hn).addAll(formParams.get(hn));
                } else {
                    l = new ArrayList<String>();
                    l.addAll(formParams.get(hn));
                    map.put(hn, l);
                }
            }
        }
        return Collections.unmodifiableMap(map);
    }

    private ServiceDefinition.METHOD mapMethod(String method) {
        if (method.equalsIgnoreCase("GET")) {
            return ServiceDefinition.METHOD.GET;
        } else if (method.equalsIgnoreCase("PUT")) {
            return ServiceDefinition.METHOD.PUT;
        } else if (method.equalsIgnoreCase("POST")) {
            return ServiceDefinition.METHOD.POST;
        } else if (method.equalsIgnoreCase("DELETE")) {
            return ServiceDefinition.METHOD.DELETE;
        } else if (method.equalsIgnoreCase("HEAD")) {
            return ServiceDefinition.METHOD.HEAD;
        } else {
            throw new IllegalStateException("Invalid Method");
        }
    }

    private Map<String, Collection<String>> mapHeaders() {
        Map<String, Collection<String>> map = new HashMap<String, Collection<String>>();
        Enumeration<String> e = request.getHeaderNames();
        String hn;
        List<String> l;
        while (e.hasMoreElements()) {
            hn = e.nextElement();
            if (map.get(hn) != null) {
                map.get(hn).add(request.getHeader(hn));
            } else {
                l = new ArrayList<String>();
                l.add(request.getHeader(hn));
                map.put(hn, l);
            }
        }
        return Collections.unmodifiableMap(map);
    }

    private Map<String, Collection<String>> mapMatrixParam(UriInfo uriInfo) {
        Map<String, Collection<String>> map = new HashMap<String, Collection<String>>();
        List<PathSegment> params = uriInfo.getPathSegments();
        for (PathSegment p : params) {
            for (String key : p.getMatrixParameters().keySet()) {
                map.put(key, p.getMatrixParameters().get(key));
            }
        }
        return Collections.unmodifiableMap(map);
    }
}
