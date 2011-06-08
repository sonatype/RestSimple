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
package org.sonatype.restsimple.sitebricks.impl;

import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.sitebricks.At;
import com.google.sitebricks.PageBinder;
import com.google.sitebricks.Show;
import com.google.sitebricks.SitebricksModule;
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
import com.google.sitebricks.routing.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.restsimple.api.ActionContext;
import org.sonatype.restsimple.api.ActionException;
import org.sonatype.restsimple.api.MediaType;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.api.ServiceHandler;
import org.sonatype.restsimple.spi.NegotiationTokenGenerator;
import org.sonatype.restsimple.spi.ResourceModuleConfig;
import org.sonatype.restsimple.spi.ServiceDefinitionGenerator;
import org.sonatype.restsimple.spi.ServiceHandlerMapper;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

/**
 * Generate a Sitebricks resource, and bind it.
 */
@Singleton
public class SitebricksServiceDefinitionGenerator implements ServiceDefinitionGenerator {

    private final ResourceModuleConfig moduleConfig;

    private final static Logger logger = LoggerFactory.getLogger(SitebricksServiceDefinitionGenerator.class);

    private SitebricksModule module;

    @Inject
    public SitebricksServiceDefinitionGenerator(ResourceModuleConfig moduleConfig) {
        this.moduleConfig = moduleConfig;
    }

    @Override
    public void generate(final ServiceDefinition serviceDefinition) {
        try {
            final String path = serviceDefinition.path().contains("/{") ? convert(serviceDefinition.path()) : serviceDefinition.path();

            if (module == null) {
                module = new com.google.sitebricks.SitebricksModule() {
                    @Override
                    protected void configureSitebricks() {
                        SitebricksServiceDefinitionGenerator.this.bind(this, path, serviceDefinition);
                        SitebricksServiceDefinitionGenerator.this.bindExtension(module, serviceDefinition);
                    }
                };
                moduleConfig.install(module);
            } else {
                bind(module, path, serviceDefinition);
                bindExtension(module, serviceDefinition);
            }
        } catch (Throwable e) {
            logger.error("generate", e);
        }
    }

    private void bind(SitebricksModule module, String path, ServiceDefinition serviceDefinition) {

        Map<String, List<ServiceHandler>> map =  sortMappingPath(serviceDefinition.serviceHandlers());
        for (Entry<String, List<ServiceHandler>> e : map.entrySet()) {
            String newPath = path.equals("/") ? e.getKey() : path + e.getKey();
            PageBinder.ShowBinder showBinder = module.at(newPath);
            for (ServiceHandler handler : e.getValue()) {
                PageBinder.ActionBinder actionBinder = showBinder.perform(mapAction(newPath, handler));

                if (handler.consumeMediaType() != null) {
                    actionBinder.selectHeader("Accept", handler.consumeMediaType().toMediaType());
                } else if (!serviceDefinition.mediaToConsume().isEmpty()) {
                    for (MediaType m : serviceDefinition.mediaToConsume()) {
                        actionBinder.selectHeader("Accept", m.toMediaType());
                    }
                }
                actionBinder.on(mapServiceToMethod(handler));
            }
        }
    }

    private Map<String, List<ServiceHandler>> sortMappingPath(List<ServiceHandler> serviceHandlers) {
         Map<String, List<ServiceHandler>> map = new HashMap<String,List<ServiceHandler>>();
         for (ServiceHandler h : serviceHandlers) {
             List<ServiceHandler> l = map.get(h.path());
             if (l == null) {
                 l = new ArrayList<ServiceHandler>();
                 map.put(h.path(), l);
             }
             l.add(h);
         }
         return map;
    }

    private void bindExtension(SitebricksModule module, ServiceDefinition serviceDefinition) {
        List<Class<?>> extensions = serviceDefinition.extensions();
        for (Class<?> clazz : extensions) {

            At at = clazz.getAnnotation(At.class);
            if (at == null) {
                throw new IllegalStateException(
                        String.format("Invalid extension %s . The clazz must be annotated with the @At annotation", clazz));
            }
            Show show = clazz.getAnnotation( Show.class );
            if (show != null) {
                module.at(at.value()).show( clazz );
            } else {
                module.at(at.value()).serve( clazz );
            }
        }
    }

    private Action mapAction(String path, ServiceHandler s) {
        switch (s.getHttpMethod()) {
            case GET:
                return new GetAction(path);
            case POST:
                return new PostAction(path);
            case PUT:
                return new PutAction(path);
            case DELETE:
                return new DeleteAction(path);
            default:
                throw new IllegalStateException("Method not supported");
        }

    }

    private final static Map<String, String> deriveNamed(Map<Integer, String> namedParams, String[] pathParam) {
        if (namedParams.isEmpty()) {
            return Collections.emptyMap();
        }

        HashMap<String, String> map = new HashMap<String,String>();
        for (int i=0; i < pathParam.length; i++) {
            boolean added = false;
            for (Map.Entry<Integer, String> e : namedParams.entrySet()) {
                if (i == e.getKey() && e.getValue().startsWith(":")) {
                    added = true;
                    map.put(e.getValue().substring(1), pathParam[i]);
                }
            }

            if (!added) {
                map.put(pathParam[i], "");
            }
        }
        return map;
    }

    public abstract static class ActionBase implements Action {
        @Inject
        protected ServiceHandlerMapper mapper;

        @Inject
        protected Provider<Request> requestProvider;

        @Inject
        protected NegotiationTokenGenerator tokenGenerator;

        protected HttpServletRequest hreq;

        protected final Map<Integer,String> namedMapping = new HashMap<Integer,String>();

        public ActionBase(String path) {
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            String[] token = path.split("/");
            for(int i = 0; i < token.length; i++) {
                namedMapping.put(i, token[i]);
            }
        }

        @Override
        public boolean shouldCall(HttpServletRequest request) {
            hreq = request;
            return true;
        }

        abstract public Object call(Object page, Map<String, String> map);

    }

    public static class PutAction extends ActionBase {

        public PutAction( String path ) {
            super( path );
        }

        public Object call(Object page, Map<String, String> map) {
            Request request = requestProvider.get();
            ServiceHandler serviceHandler = mapper.map("put", hreq.getServletPath());

            if (serviceHandler == null) {
                return Reply.with("Method not allowed").status(405);
            }

            Object body = readBody(serviceHandler, request);
            String p = hreq.getServletPath();
            if (p.startsWith("/")) {
                p = p.substring(1);
            }
            Map<String,String> pathParams = deriveNamed(namedMapping, p.split("/"));
            Object response = createResponse(tokenGenerator, "put", hreq.getServletPath(), pathParams, body, request, mapper);

            if (response == null) {
                return Reply.NO_REPLY.noContent();
            } else if (Reply.class.isAssignableFrom(response.getClass())) {
                return Reply.class.cast(response);
            }

            return serializeResponse(request, response).status(201);
        }
    }

    public static class PostAction extends ActionBase {

        public PostAction( String path ) {
            super( path );
        }

        @Override
        public Object call(Object page, Map<String, String> map) {
            Request request = requestProvider.get();
            ServiceHandler serviceHandler = mapper.map("post", hreq.getServletPath());

            if (serviceHandler == null) {
                return Reply.with("Method not allowed").status(405);
            }

            Object body = readBody(serviceHandler, request);
            String p = hreq.getServletPath();
            if (p.startsWith("/")) {
                p = p.substring(1);
            }
            Map<String,String> pathParams = deriveNamed(namedMapping, p.split("/"));
            Object response = createResponse(tokenGenerator, "post", hreq.getServletPath(), pathParams, body, request, mapper);

            if (Reply.class.isAssignableFrom(response.getClass())) {
                return Reply.class.cast(response);
            }
            return serializeResponse(request, response);
        }
    }

    public static class GetAction extends ActionBase {

        public GetAction( String path ) {
            super( path );
        }

        @Override
        public Object call(Object page, Map<String, String> map) {

            Request request = requestProvider.get();
            String p = hreq.getServletPath();
            if (p.startsWith("/")) {
                p = p.substring(1);
            }
            Map<String,String> pathParams = deriveNamed(namedMapping, p.split("/"));
            Object response = createResponse(tokenGenerator, "get", hreq.getServletPath(), pathParams, null, request, mapper);

            if (response == null) {
                return Reply.NO_REPLY.noContent();
            } else if (Reply.class.isAssignableFrom(response.getClass())) {
                return Reply.class.cast(response);
            }

            return serializeResponse(request, response);
        }
    }

    public static class DeleteAction extends ActionBase {

        public DeleteAction( String path ) {
            super( path );
        }

        @Override
        public Object call(Object page, Map<String, String> map) {
            Request request = requestProvider.get();
            String p = hreq.getServletPath();
            if (p.startsWith("/")) {
                p = p.substring(1);
            }
            Map<String,String> pathParams = deriveNamed(namedMapping, p.split("/"));
            Object response = createResponse(tokenGenerator, "delete", hreq.getServletPath(), pathParams, null, request, mapper);
            if (response == null) {
                return Reply.NO_REPLY.noContent();
            } else if (Reply.class.isAssignableFrom(response.getClass())) {
                return Reply.class.cast(response);
            }

            return serializeResponse(request, response);
        }
    }

    private Class<? extends Annotation> mapServiceToMethod(ServiceHandler s) {
        switch (s.getHttpMethod()) {
            case GET:
                return Get.class;
            case POST:
                return Post.class;
            case PUT:
                return Put.class;
            case DELETE:
                return Delete.class;
            default:
                throw new IllegalStateException("Method not supported");
        }
    }

    private static Reply<?> serializeResponse(Request request, Object response) {
        Collection<String> c = request.headers().get("Accept");

        String accept = c.size() > 0 ? c.iterator().next() : null;

        boolean wildCard = false;
        if (accept.equalsIgnoreCase("*/*")) {
            wildCard = true;
        }

        if (response == null) {
            return Reply.with("").noContent();
        } else if (accept != null) {
            Map<String, String> m = new HashMap<String, String>();
            // Default to JSON
            if (wildCard) {
                m.put("Content-Type", "text/json");
                return Reply.with(response).headers(m).as(Json.class);
            } else if (accept.endsWith("json")) {
                m.put("Content-Type", "application/json");
                return Reply.with(response).headers(m).as(Json.class);
            } else if (accept.endsWith("xml")) {
                m.put("Content-Type", "application/xml");
                return Reply.with(response).headers(m).as(Xml.class);
            }
        }
        return Reply.with(response.toString()).as(Text.class);
    }

    private static <T> Object createResponse(NegotiationTokenGenerator tokenGenerator,
                                             String methodName,
                                             String servletPath,
                                             Map<String,String> pathParams,
                                             T body,
                                             Request request,
                                             ServiceHandlerMapper mapper) {

        ServiceHandler serviceHandler = mapper.map(methodName, convertToJaxRs(servletPath));

        if (serviceHandler == null) {
            return Reply.with("Method not allowed").status(405);
        }

        if (!contentNegotiate(request.headers(), serviceHandler.mediaToProduce())) {
            Map<String, String> m = new HashMap<String, String>();
            m.put(tokenGenerator.challengedHeaderName(),
                    tokenGenerator.generateNegotiationHeader(servletPath, serviceHandler.mediaToProduce()));

            return Reply.with("Not Acceptable").headers(m).status(406);
        }

        if (!serviceHandler.getHttpMethod().name().equalsIgnoreCase(methodName)) {
            return Reply.with("Method not allowed").status(405);
        }

        if (body == null) {
            body = (T) "";
        }

        Object response = null;
        org.sonatype.restsimple.api.Action action = serviceHandler.getAction();
        try {
            ActionContext<T> actionContext = new ActionContext<T>(mapMethod(methodName),
                    mapHeaders(request.headers()),
                    mapFormParams(request.params()),
                    mapMatrixParams(request.matrix()),
                    new ByteArrayInputStream(body.toString().getBytes()),
                    pathParams,
                    body);

            response = action.action(actionContext);
        } catch (ActionException e) {
            return Reply.with(e).status(e.getStatusCode());
        } catch (Throwable e) {
            logger.debug("ActionContext error", e);
            return Reply.with(e).error();
        }
        return response;
    }

    private static boolean contentNegotiate(Multimap<String, String> headers, List<MediaType> mediaTypes) {

        if (mediaTypes.size() == 0) {
            return true;
        }

        //TODO: Wildcard support
        for (Map.Entry<String, String> e : headers.entries()) {
            if (e.getKey().equalsIgnoreCase("Accept")) {
                if (e.getValue().trim().equals("*/*")) {
                    return true;
                }

                for (MediaType mediaType : mediaTypes) {
                    if (mediaType.toMediaType().equalsIgnoreCase(e.getValue())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static Map<String, Collection<String>> mapMatrixParams(Multimap<String, String> matrixParams) {
        Map<String, Collection<String>> map = new HashMap<String, Collection<String>>();
        if (matrixParams != null) {
            for (Map.Entry<String, String> e : matrixParams.entries()) {
                ArrayList<String> list = new ArrayList<String>();
                list.add(e.getValue());
                map.put(e.getKey(), list);
            }
        }
        return Collections.unmodifiableMap(map);
    }

    private static Map<String, Collection<String>> mapFormParams(Multimap<String, String> formParams) {
        Map<String, Collection<String>> map = new HashMap<String, Collection<String>>();
        if (formParams != null) {
            for (Map.Entry<String, String> e : formParams.entries()) {
                ArrayList<String> list = new ArrayList<String>();
                list.add(e.getValue());
                map.put(e.getKey(), list);
            }
        }
        return Collections.unmodifiableMap(map);
    }

    private static ServiceDefinition.METHOD mapMethod(String method) {
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

    private static Map<String, Collection<String>> mapHeaders(Multimap<String, String> gMap) {
        Map<String, Collection<String>> map = new HashMap<String, Collection<String>>();
        for (Map.Entry<String, String> e : gMap.entries()) {
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

    private static String convertToJaxRs(String path) {
        StringTokenizer st = new StringTokenizer(path, "/");
        StringBuilder newPath = new StringBuilder();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.startsWith(":")) {
                newPath.append("/").append(token.replace(":", "{")).append("}");
            } else {
                newPath.append("/").append(token);
            }
        }
        return newPath.toString();
    }

    private static Object readBody(ServiceHandler serviceHandler, Request request) {
        Class<? extends Transport> transport = Text.class;
        String contentSubType = request.header("Content-Type");
        if (contentSubType != null && contentSubType.indexOf("/") > 0) {
            contentSubType = contentSubType.substring(contentSubType.indexOf("/") + 1);
        }

        String subType = serviceHandler.consumeMediaType() == null ? contentSubType : serviceHandler.consumeMediaType().subType();
        if (subType != null && subType.endsWith("json")) {
            transport = Json.class;
        } else if (subType != null && subType.endsWith("xml")) {
            transport = Xml.class;
        }

        Object body = null;

        Class<?> c = serviceHandler.consumeClass() != null ? serviceHandler.consumeClass() : String.class;
        try {
            body = request.read(c).as(transport);
        } catch (Exception ex) {
            logger.trace("readBody parse exception", ex);
        }

        return body;
    }

    private String convert(String path) {
        StringTokenizer st = new StringTokenizer(path, "/");
        StringBuilder newPath = new StringBuilder();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.startsWith("{")) {
                newPath.append("/").append(token.replace("{", ":").substring(0, newPath.length() - 1));
            } else {
                newPath.append("/").append(token);
            }
        }

        String s = newPath.toString();
        if (s.isEmpty()) {
            s = "/";
        }
        return s;
    }

}





