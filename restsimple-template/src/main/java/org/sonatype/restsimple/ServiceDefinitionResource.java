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

    @GET
    public Object get(@PathParam("method") String service, @PathParam("id") String value) {
        logger.debug("HTTP GET: Generated Resource invocation for method {} with id {}", service, value);
        Object response = invokeAction("get", service, value, null, null);
        return response;
    }

    @HEAD
    public Response head(@PathParam("method") String service, @PathParam("id") String value) {
        logger.debug("HTTP HEAD: Generated Resource invocation for method {} with id {}", service, value);
        Object response = invokeAction("head", service, value, null, null);
        return Response.ok().build();
    }

    @PUT
        @Consumes("application/vnd.org.sonatype.rest+json")
    @Produces("application/vnd.org.sonatype.rest+json")
    public Response put(@Context UriInfo uriInfo, @PathParam("method") String service, @PathParam("id") String value, Object jacksonObject) {
        logger.debug("HTTP POST: Generated Resource invocation for method {} with id {} and id {} ", service, value);

        URI location = UriBuilder.fromResource(getClass()).build(new String[]{"", "", ""});
        Object response = invokeAction("put", service, value, uriInfo.getQueryParameters(), jacksonObject);
        return Response.created(location).entity(response).build();
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    public Response post(@PathParam("method") String service, @PathParam("id") String value, MultivaluedMap<String, String> formParams) {
        logger.debug("HTTP POST: Generated Resource invocation for method {} with id {} and update {}", service, value);
        Object response = invokeAction("post", service, value, formParams, null);
        if (response == null) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.ok(response).build();
        }
    }

    @POST
    @Consumes("application/vnd.org.sonatype.rest+json")
    @Produces("application/vnd.org.sonatype.rest+json")
    public Response postWithBody(@Context UriInfo uriInfo, @PathParam("method") String service, @PathParam("id") String value, Object jacksonObject) {
        logger.debug("HTTP POST: Generated Resource invocation for method {} with id {} and id {} ", service, value);

        Object response = invokeAction("post", service, value, uriInfo.getQueryParameters(), jacksonObject);
        if (response == null) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.ok(response).build();
        }
    }

    @DELETE
    public Response delete(@PathParam("method") String service, @PathParam("id") String value) {
        logger.debug("HTTP DELETE: Generated Resource invocation for method {} with id {}", service, value);
        Object response = invokeAction("delete", service, value, null, null);
        return Response.ok(response).build();
    }

    private <T> Object invokeAction(String methodName, String pathName, String pathValue, MultivaluedMap<String, String> formParams, T body) {
        ServiceHandler serviceHandler = mapper.map(pathName);
        if (serviceHandler == null) {
            throw new WebApplicationException(new IllegalStateException("No ServiceHandler defined for service " + pathName));
        }

        if (!serviceHandler.getHttpMethod().name().equalsIgnoreCase(methodName)) {
            throw new WebApplicationException(Response.status(405).entity("Method not allowed").build());
        }

        Object response = null;
        Action action = serviceHandler.getAction();
        try {
            ActionContext<T> actionContext = new ActionContext<T>(mapMethod(request.getMethod()), mapHeaders(),
                    mapFormParams(formParams), request.getInputStream(), pathName, pathValue, body);
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
            for(String hn : formParams.keySet()) {
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
        return map;
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
}
