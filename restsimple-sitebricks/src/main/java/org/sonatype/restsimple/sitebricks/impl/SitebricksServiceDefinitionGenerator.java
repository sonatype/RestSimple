/*
 * Copyright (c) 2011 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.restsimple.sitebricks.impl;

import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.sitebricks.PageBinder;
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
import org.sonatype.restsimple.api.MediaType;
import org.sonatype.restsimple.spi.NegotiationTokenGenerator;
import org.sonatype.restsimple.spi.RFC2295NegotiationTokenGenerator;
import org.sonatype.restsimple.spi.ResourceModuleConfig;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.api.ServiceHandler;
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
import java.util.StringTokenizer;

/**
 * Generate a Sitebricks resource, and bind it.
 */
@Singleton
public class SitebricksServiceDefinitionGenerator implements ServiceDefinitionGenerator {

    private final ResourceModuleConfig moduleConfig;

    private final static Logger logger = LoggerFactory.getLogger(SitebricksServiceDefinitionGenerator.class);

    private SitebricksModule module;

    private final NegotiationTokenGenerator negotiationTokenGenerator;

    @Inject
    public SitebricksServiceDefinitionGenerator(ResourceModuleConfig moduleConfig, NegotiationTokenGenerator negotiationTokenGenerator) {
        this.moduleConfig = moduleConfig;
        this.negotiationTokenGenerator = negotiationTokenGenerator;
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
        return newPath.toString();
    }

    @Override
    public void generate(final ServiceDefinition serviceDefinition) {
        try {
            final String path = serviceDefinition.path().contains("/{") ? convert(serviceDefinition.path()) : serviceDefinition.path();
            //TODO: this is extremely ugly
            moduleConfig.bindTo(NegotiationTokenGenerator.class, negotiationTokenGenerator.getClass());

            if (module == null) {
                module = new com.google.sitebricks.SitebricksModule() {
                    @Override
                    protected void configureSitebricks() {
                        SitebricksServiceDefinitionGenerator.this.bind(this, path, serviceDefinition);
                    }
                };
                moduleConfig.install(module);
            } else {
                bind(module, path, serviceDefinition);
            }
        } catch (Throwable e) {
            logger.error("generate", e);
        }
    }

    private void bind(SitebricksModule module, String path, ServiceDefinition serviceDefinition) {
        PageBinder.ShowBinder showBinder = module.at(path.equals("/") ? "/:method/:id" : path + "/:method/:id");

        for (ServiceHandler handler : serviceDefinition.serviceHandlers()) {

            PageBinder.ActionBinder actionBinder = showBinder.perform(mapAction(handler));

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

    private Class<? extends Action> mapAction(ServiceHandler s) {
        switch (s.getHttpMethod()) {
            case GET:
                return GetAction.class;
            case POST:
                return PostAction.class;
            case PUT:
                return PutAction.class;
            case DELETE:
                return DeleteAction.class;
            default:
                throw new IllegalStateException("Method not supported");
        }

    }

    public static class PutAction implements Action {

        @Inject
        private ServiceHandlerMapper mapper;

        @Inject
        private Provider<Request> requestProvider;

        @Inject
        NegotiationTokenGenerator tokenGenerator;

        @Override
        public boolean shouldCall(HttpServletRequest request) {
            return true;
        }

        public Object call(Object page, Map<String, String> map) {
            Request request = requestProvider.get();
            Object response = createResponse(tokenGenerator, "put", map.get("method"), map.get("id"), null, request, mapper);

            if (response == null) {
                return Reply.NO_REPLY.noContent();
            } else if (Reply.class.isAssignableFrom(response.getClass())) {
                return Reply.class.cast(response);
            }

            return serializeResponse(request, response).status(201);
        }
    }

    public static class PostAction implements Action {

        @Inject
        ServiceHandlerMapper mapper;

        @Inject
        Provider<Request> requestProvider;

        @Inject
        NegotiationTokenGenerator tokenGenerator;

        @Override
        public boolean shouldCall(HttpServletRequest request) {
            return true;
        }

        @Override
        public Object call(Object page, Map<String, String> map) {
            Request request = requestProvider.get();
            ServiceHandler serviceHandler = mapper.map(convertToJaxRs(map.get("method")));
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
            Object response = createResponse(tokenGenerator, "post", map.get("method"), map.get("id"), body, request, mapper);

            if (Reply.class.isAssignableFrom(response.getClass())) {
                return Reply.class.cast(response);
            }
            return serializeResponse(request, response);
        }
    }

    public static class GetAction implements Action {

        @Inject
        ServiceHandlerMapper mapper;

        @Inject
        Provider<Request> requestProvider;

        @Inject
        NegotiationTokenGenerator tokenGenerator;

        @Override
        public boolean shouldCall(HttpServletRequest request) {
            return true;
        }

        @Override
        public Object call(Object page, Map<String, String> map) {

            Request request = requestProvider.get();
            Object response = createResponse(tokenGenerator, "get", map.get("method"), map.get("id"), null, request, mapper);

            if (response == null) {
                return Reply.NO_REPLY.noContent();
            } else if (Reply.class.isAssignableFrom(response.getClass())) {
                return Reply.class.cast(response);
            }

            return serializeResponse(request, response);
        }
    }

    public static class DeleteAction implements Action {

        @Inject
        ServiceHandlerMapper mapper;

        @Inject
        Provider<Request> requestProvider;

        @Inject
        NegotiationTokenGenerator tokenGenerator;

        @Override
        public boolean shouldCall(HttpServletRequest request) {
            return true;
        }

        @Override
        public Object call(Object page, Map<String, String> map) {
            Request request = requestProvider.get();

            Object response = createResponse(tokenGenerator, "delete", map.get("method"), map.get("id"), null, request, mapper);
            if (response == null) {
                return Reply.NO_REPLY.noContent();
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

        String contentType = c.size() > 0 ? c.iterator().next() : null;
        if (response == null) {
            return Reply.with("").noContent();
        } else if (contentType != null) {
            if (contentType.endsWith("json")) {
                Map<String, String> m = new HashMap<String, String>();
                m.put("Content-Type", "application/json");
                return Reply.with(response).headers(m).as(Json.class);
            } else if (contentType.endsWith("xml")) {
                Map<String, String> m = new HashMap<String, String>();
                m.put("Content-Type", "application/xml");
                return Reply.with(response).as(Xml.class);
            }
        }
        return Reply.with(response.toString()).as(Text.class);
    }

    private static <T> Object createResponse(NegotiationTokenGenerator tokenGenerator, String methodName, String pathName, String pathValue, T body, Request request, ServiceHandlerMapper mapper) {
        ServiceHandler serviceHandler = mapper.map(convertToJaxRs(pathName));

        if (serviceHandler == null) {
            return Reply.with("No ServiceHandler defined for service " + pathName).error();
        }

        if (!contentNegotiate(request.headers(), serviceHandler.mediaToProduce())) {
            Map<String, String> m = new HashMap<String, String>();
            m.put("Alternates", tokenGenerator.generateNegotiationHeader(pathName + "/" + pathValue, serviceHandler.mediaToProduce()));
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
            ActionContext<T> actionContext = new ActionContext<T>(mapMethod(methodName), mapHeaders(request.headers()),
                    mapFormParams(request.params()), new ByteArrayInputStream(body.toString().getBytes()), pathName, pathValue, body);
            response = action.action(actionContext);
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

        for (Map.Entry<String, String> e : headers.entries()) {
            if (e.getKey().equalsIgnoreCase("Accept")) {
                for (MediaType mediaType : mediaTypes) {
                    if (mediaType.toMediaType().equalsIgnoreCase(e.getValue())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static Map<String, Collection<String>> mapFormParams(Multimap<String, String> formParams) {
        Map<String, Collection<String>> map = new HashMap<String, Collection<String>>();
        if (formParams != null) {
            for (Map.Entry<String, String> e : formParams.entries()) {
                ArrayList<String> list = new ArrayList<String>();
                list.add(e.getValue());
                map.put(e.getKey(), list);
            }
            return Collections.unmodifiableMap(map);
        }
        return map;
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
}





