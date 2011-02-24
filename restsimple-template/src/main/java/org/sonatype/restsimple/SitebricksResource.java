package org.sonatype.restsimple;

import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.sitebricks.At;
import com.google.sitebricks.client.Transport;
import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.client.transport.Text;
import com.google.sitebricks.client.transport.Xml;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.http.Delete;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Post;
import com.google.sitebricks.http.Put;
import com.google.sitebricks.http.negotiate.Accept;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.restsimple.api.Action;
import org.sonatype.restsimple.api.ActionContext;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.api.ServiceHandler;
import org.sonatype.restsimple.spi.ServiceHandlerMapper;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This is what we want to generate automatically using ASM.
 * <p/>
 * NOTE: This class is not used, but we do instead generate some form ot it based on {@link org.sonatype.restsimple.api.ServiceDefinition}
 */
@At("/:path/:method/:id")
public final class SitebricksResource {

    private Logger logger = LoggerFactory.getLogger(SitebricksResource.class);

    @Inject
    ServiceHandlerMapper mapper;

    @Get
    @Accept("application/vnd.org.sonatype.rest+json")
    public Reply<?> get(@Named("method") String service, @Named("id") String value, Request request) {
        logger.debug("HTTP GET: Generated Resource invocation for method {} with id {}", service, value);
        Object response = createResponse("get", service, value, null, request);

        if (Reply.class.isAssignableFrom(response.getClass())) {
            return Reply.class.cast(response);
        }

        return serializeResponse(request, response);
    }

    @Put
    @Accept("application/vnd.org.sonatype.rest+json")
    public Reply<?> put(@Named("method") String service, @Named("id") String value, Request request) {
        logger.debug("HTTP PUT: Generated Resource invocation for method {} with id {}", service, value);
        Object response = createResponse("put", service, value, null, request);

        if (Reply.class.isAssignableFrom(response.getClass())) {
            return Reply.class.cast(response);
        }

        return serializeResponse(request, response).status(201);
    }

    @Post
    @Accept("application/vnd.org.sonatype.rest+json")
    public Reply<?> post0(@Named("method") String service, @Named("id") String value, Request request) {
        logger.debug("HTTP POST: Generated Resource invocation for method {} with id {} and update {}", service, value);
        Object response = createResponse("post", service, value, null, request);

        if (Reply.class.isAssignableFrom(response.getClass())) {
            return Reply.class.cast(response);
        }

        return serializeResponse(request, response);
    }

    @Post
    @Accept("application/vnd.org.sonatype.rest+json")
    public Reply<?> post(@Named("method") String service, @Named("id") String value, Request request) {
        logger.debug("HTTP POST: Generated Resource invocation for method {} with id {} and update {}", service, value);

        ServiceHandler serviceHandler = mapper.map(service);
        Class<? extends Transport> transport = Text.class;
        String subType = serviceHandler.consumeMediaType() == null ? null : serviceHandler.consumeMediaType().subType();
        if (subType != null && subType.endsWith("json")) {
            transport = Json.class;
        } else if (subType != null && subType.endsWith("xml")) {
            transport = Xml.class;
        }
        
        Object body = null;

        if (serviceHandler.consumeClass() != null) {
            body = request.read(serviceHandler.consumeClass()).as(transport);
        }
        Object response = createResponse("post", service, value, body, request);

        if (Reply.class.isAssignableFrom(response.getClass())) {
            return Reply.class.cast(response);
        }
        return serializeResponse(request, response);
    }

    @Delete
    @Accept("application/vnd.org.sonatype.rest+json")
    public Reply<?> delete(@Named("method") String service, @Named("id") String value, Request request) {
        logger.debug("HTTP DELETE: Generated Resource invocation for method {} with id {}", service, value);
        Object response = createResponse("delete", service, value, null, request);
        return serializeResponse(request, response);
    }

    private Reply<?> serializeResponse(Request request, Object response) {
        Collection<String> c = request.headers().get("Accept");

        String contentType = c.size() > 0 ? c.iterator().next() : null;
        if (response == null) {
            return Reply.with("").noContent();
        } else if (contentType != null) {
            if (contentType.endsWith("json")) {
                return Reply.with(response).as(Json.class);
            } else if (contentType.endsWith("xml")) {
                return Reply.with(response).as(Xml.class);
            }
        }
        return Reply.with(response.toString()).as(Text.class);
    }

    private <T> Object createResponse(String methodName, String pathName, String pathValue, T body, Request request) {
        ServiceHandler serviceHandler = mapper.map(pathName);
        if (serviceHandler == null) {
            return Reply.with("No ServiceHandler defined for service " + pathName).error();
        }

        if (!serviceHandler.getHttpMethod().name().equalsIgnoreCase(methodName)) {
            return Reply.with("Method not allowed").status(405);
        }

        if (body == null) {
            body = (T) "";
        }

        Object response = null;
        Action action = serviceHandler.getAction();
        try {
            ActionContext<T> actionContext = new ActionContext<T>(mapMethod(methodName), mapHeaders(request.headers()),
                    mapFormParams(request.params()), new ByteArrayInputStream(body.toString().getBytes()), pathName, pathValue, body);
            response = action.action(actionContext);
        }  catch (Throwable e) {
            logger.error("delegate", e);
            return Reply.with(e).error();
        }
        return response;
    }


    private Map<String, Collection<String>> mapFormParams(Multimap<String, String> formParams) {
        Map<String, Collection<String>> map = new HashMap<String, Collection<String>>();
        if (formParams != null) {
            for( Map.Entry<String,String> e: formParams.entries()) {
                ArrayList<String> list = new ArrayList<String>();
                list.add(e.getValue());
                map.put(e.getKey(),list);
            }
            return Collections.unmodifiableMap(map);
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

    private Map<String, Collection<String>> mapHeaders(Multimap<String, String> gMap) {
        Map<String, Collection<String>> map = new HashMap<String, Collection<String>>();
        for( Map.Entry<String,String> e: gMap.entries()) {
            if (map.get(e.getKey()) != null) {
                map.get(e.getKey()).add(e.getValue());
            } else {
                ArrayList<String> list = new ArrayList<String>();
                list.add(e.getValue());
                map.put(e.getKey(), list);
            }
        }
        return Collections.unmodifiableMap(map);
    }
}


