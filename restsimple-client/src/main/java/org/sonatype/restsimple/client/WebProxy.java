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
package org.sonatype.restsimple.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.restsimple.api.Action;
import org.sonatype.restsimple.api.ActionContext;
import org.sonatype.restsimple.api.ActionException;
import org.sonatype.restsimple.api.DefaultServiceDefinition;
import org.sonatype.restsimple.api.DeleteServiceHandler;
import org.sonatype.restsimple.api.GetServiceHandler;
import org.sonatype.restsimple.api.MediaType;
import org.sonatype.restsimple.api.PostServiceHandler;
import org.sonatype.restsimple.api.PutServiceHandler;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.api.ServiceHandler;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple proxy that generates RestSimple client from an annotated interface. jsr 311 annotations are all supported.
 * As simple as {code
 * <p/>
 * public static interface ProxyClient {
 *
 * @GET
 * @Path("getPet")
 * @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
 * public Pet get(@PathParam("myPet") String path);
 * @GET
 * @Path("getPetString")
 * @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
 * public String getString(@PathParam("myPet") String path);
 * @POST
 * @Path("addPet")
 * @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
 * public Pet post(@PathParam("myPet") String myPet, String body);
 * @DELETE
 * @Path("deletePet")
 * @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
 * public Pet delete(@PathParam("myPet") String path);
 * <p/>
 * }
 * <p/>
 * ProxyClient client = WebProxy.createProxy(ProxyClient.class, URI.create("http://someurl/));
 * Pet pet = client.post("myPet", "{\"name\":\"pouetpouet\"}");
 * <p/>
 * }
 */
public class WebProxy {

    private final static Logger logger = LoggerFactory.getLogger(WebProxy.class);

    public static final <T> T createProxy(Class<T> clazz, URI uri) {

        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
                new WebProxyHandler(uri, createServiceDefinitionInfo(clazz)));
    }

    private static class WebProxyHandler implements InvocationHandler {

        private final ServiceDefinitionInfo serviceDefinitionInfo;
        private final URI uri;
        private final Web web;

        public WebProxyHandler(URI uri, ServiceDefinitionInfo serviceDefinitionInfo) {
            this.serviceDefinitionInfo = serviceDefinitionInfo;
            this.uri = uri;
            this.web = new Web(serviceDefinitionInfo.serviceDefinition());
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            HttpMethodInfo inf = serviceDefinitionInfo.methodsMap.get(method.getName());
            ServiceDefinition.METHOD m = inf.getMethodType();
            if (m == null) {
                throw new IllegalStateException(String.format("Unable to proxy method %s", method.getName()));
            }

            String rootPath = inf.getRootPath();
            String path = inf.getPath();

            String uriString = uri.toURL().toString();
            StringBuilder builder = new StringBuilder(uriString);
            if (!uriString.endsWith("/") && path != null) {
                builder.append("/");
            }

            if (!rootPath.equals("")) {
                if (rootPath.startsWith("/")) {
                    builder.append(rootPath.substring(1));
                } else {
                    builder.append(rootPath);
                }

                if (!rootPath.endsWith("/")) {
                    builder.append("/");
                }
            }

            if (path != null) {
                if (path.startsWith("/")) {
                    builder.append(path.substring(1));
                } else {
                    builder.append(path);
                }
            }

            String body = retrieveBody(inf.getMethod(), args);
            switch (m) {
                case GET:
                    return web.clientOf(builder.toString())
                            .headers(constructCookie(inf.getMethod(), args, constructHeaders(inf.getMethod(), args)))
                            .queryString(constructFormString(inf.getMethod(), args, constructQueryString(inf.getMethod(), args)))
                            .get(inf.getReturnClassType());
                case POST:
                    return web.clientOf(builder.toString())
                            .headers(constructCookie(inf.getMethod(), args, constructHeaders(inf.getMethod(), args)))
                            .queryString(constructFormString(inf.getMethod(), args, constructQueryString(inf.getMethod(), args)))
                            .post(body, inf.getReturnClassType());
                case DELETE:
                    return web.clientOf(builder.toString())
                            .headers(constructCookie(inf.getMethod(), args, constructHeaders(inf.getMethod(), args)))
                            .queryString(constructFormString(inf.getMethod(), args, constructQueryString(inf.getMethod(), args)))
                            .delete(body, inf.getReturnClassType());
                case PUT:
                    return web.clientOf(builder.toString())
                            .headers(constructCookie(inf.getMethod(), args, constructHeaders(inf.getMethod(), args)))
                            .queryString(constructFormString(inf.getMethod(), args, constructQueryString(inf.getMethod(), args)))
                            .put(body, inf.getReturnClassType());
                default:
                    throw new IllegalStateException(String.format("Invalid Method type %s", m));
            }
        }

        private String retrieveBody(Method m, Object params[]) {
            Annotation[][] ans = m.getParameterAnnotations();

            int i = 0;
            for (Annotation[] annotations : ans) {
                if (annotations.length == 0) {
                    return params[i].toString();
                }
                i++;
            }
            return "";
        }

        private Map<String, String> constructHeaders(Method m, Object params[]) {
            Map<String, String> headers = new HashMap<String, String>();
            Annotation[][] ans = m.getParameterAnnotations();

            int i = 0;
            for (Annotation[] annotations : ans) {
                for (Annotation a : annotations) {
                    if (HeaderParam.class.isAssignableFrom(a.getClass())) {
                        logger.debug("Processing @HeaderParam {}", a);
                        headers.put(HeaderParam.class.cast(a).value(), params[i].toString());
                    }
                    i++;
                }
            }
            return headers;
        }

        private Map<String, String> constructCookie(Method m, Object params[], Map<String, String> headers) {
            Annotation[][] ans = m.getParameterAnnotations();

            int i = 0;
            for (Annotation[] annotations : ans) {
                for (Annotation a : annotations) {
                    if (CookieParam.class.isAssignableFrom(a.getClass())) {
                        logger.debug("Processing @CookieParam {}", a);
                        headers.put("Cookie", params[i].toString());
                    }
                    i++;
                }
            }
            return headers;
        }

        private Map<String, String> constructQueryString(Method m, Object params[]) {
            Map<String, String> queryStrings = new HashMap<String, String>();
            Annotation[][] ans = m.getParameterAnnotations();

            int i = 0;
            for (Annotation[] annotations : ans) {
                for (Annotation a : annotations) {
                    if (QueryParam.class.isAssignableFrom(a.getClass())) {
                        logger.debug("Processing @QueryParam {}", a);
                        queryStrings.put(QueryParam.class.cast(a).value(), params[i].toString());
                    }
                    i++;
                }
            }
            return queryStrings;
        }

        private Map<String, String> constructFormString(Method m, Object params[], Map<String, String> queryStrings) {
            Annotation[][] ans = m.getParameterAnnotations();

            int i = 0;
            for (Annotation[] annotations : ans) {
                for (Annotation a : annotations) {
                    if (FormParam.class.isAssignableFrom(a.getClass())) {
                        logger.debug("Processing @FormParam {}", a);
                        queryStrings.put(FormParam.class.cast(a).value(), params[i].toString());
                    }
                    i++;
                }
            }
            return queryStrings;
        }
    }

    /**
     * \// TODO: Better design should use the visitor pattern.
     *
     * @param clazz
     * @return
     */
    private static ServiceDefinitionInfo createServiceDefinitionInfo(Class<?> clazz) {

        Path rootPath = clazz.getAnnotation(Path.class);
        ServiceDefinition sd = new DefaultServiceDefinition();
        String rootPathString = "";
        if (rootPath != null) {
            sd.withPath(rootPath.value());
            rootPathString = rootPath.value();
        } 

        Consumes topLevelConsumes = clazz.getAnnotation(Consumes.class);
        if (topLevelConsumes != null) {
            for (String c : topLevelConsumes.value()) {
                logger.debug("Processing @Consumes {}", c);
                sd.consuming(new MediaType(getType(c), getSubType(c)));
            }
        }

        Produces topLevelProduces = clazz.getAnnotation(Produces.class);
        if (topLevelProduces != null) {
            for (String p : topLevelProduces.value()) {
                logger.debug("Processing @Produces {}", p);
                sd.producing(new MediaType(getType(p), getSubType(p)));
            }
        }

        Method[] method = clazz.getMethods();
        ServiceHandler sh = null;
        HashMap<String, HttpMethodInfo> methodsMap = new HashMap<String, HttpMethodInfo>();
        for (Method m : method) {
            logger.debug("Processing method {}", m);
            boolean found = false;
            for (Annotation a : m.getAnnotations()) {
                logger.debug("Processing annotation {}", a);

                Path path = m.getAnnotation(Path.class);
                if (path == null) {
                    throw new IllegalStateException("@Path annotation missing");
                }

                HttpMethodInfo httpMethodInfo = null;
                String pathValue = constructPath(m, path.value());

                if (GET.class.isAssignableFrom(a.getClass())) {
                    sh = new GetServiceHandler(pathValue, new DummyAction());
                    httpMethodInfo = new HttpMethodInfo(ServiceDefinition.METHOD.GET,
                            pathValue,
                            m,
                            rootPathString);
                    found = true;
                } else if (POST.class.isAssignableFrom(a.getClass())) {
                    sh = new PostServiceHandler(pathValue, new DummyAction());
                    httpMethodInfo = new HttpMethodInfo(ServiceDefinition.METHOD.POST,
                            pathValue,
                            m,
                            rootPathString);
                    found = true;
                } else if (PUT.class.isAssignableFrom(a.getClass())) {
                    sh = new PutServiceHandler(pathValue, new DummyAction());
                    httpMethodInfo = new HttpMethodInfo(ServiceDefinition.METHOD.PUT,
                            pathValue,
                            m,
                            rootPathString);
                    found = true;
                } else if (DELETE.class.isAssignableFrom(a.getClass())) {
                    sh = new DeleteServiceHandler(pathValue, new DummyAction());
                    httpMethodInfo = new HttpMethodInfo(ServiceDefinition.METHOD.DELETE,
                            pathValue,
                            m, 
                            rootPathString);
                    found = true;
                }

                if (found) {
                    sd.withHandler(sh);

                    Produces produces = m.getAnnotation(Produces.class);
                    if (produces != null) {
                        for (String p : produces.value()) {
                            logger.debug("Processing @Produces {}", p);
                            sh.producing(new MediaType(getType(p), getSubType(p)));
                        }
                    }

                    Consumes consumes = m.getAnnotation(Consumes.class);
                    if (consumes != null) {
                        for (String c : consumes.value()) {
                            // TODO
                            logger.debug("Processing @Consumes {}", c);
                            sh.consumeWith(new MediaType(getType(c), getSubType(c)), null);
                        }
                    }

                    methodsMap.put(m.getName(), httpMethodInfo);
                    break;
                }
            }
            if (!found) {
                throw new IllegalStateException("Method not supported:  " + m);
            }
        }
        return new ServiceDefinitionInfo(sd, methodsMap);
    }

    private final static String constructPath(Method m, String path) {
        StringBuilder pathBuilder = new StringBuilder(path);
        Annotation[][] ans = m.getParameterAnnotations();

        for (Annotation[] annotations : ans) {
            for (Annotation a : annotations) {
                if (PathParam.class.isAssignableFrom(a.getClass())) {
                    logger.debug("Processing @PathParam {}", a);
                    pathBuilder.append("/").append(PathParam.class.cast(a).value());
                }
            }
        }
        return pathBuilder.toString();
    }

    private static final String getType(String mediaType) {
        int index = mediaType.indexOf("/");
        if (index < 1) {
            return mediaType;
        }
        return mediaType.substring(0, index);
    }

    private static final String getSubType(String mediaType) {
        int index = mediaType.indexOf("/");
        if (index < 1) {
            return mediaType;
        }
        return mediaType.substring(index + 1);
    }

    private static final class DummyAction implements Action {

        @Override
        public Object action(ActionContext actionContext) throws ActionException {
            return null;
        }
    }

    private static final class HttpMethodInfo {

        private final ServiceDefinition.METHOD methodType;
        private final String path;
        private final Method method;
        private final String rootPath;

        public HttpMethodInfo(ServiceDefinition.METHOD methodType,
                              String path,
                              Method method,
                              String rootPath) {

            this.methodType = methodType;
            this.path = path;
            this.method = method;
            this.rootPath = rootPath;
        }

        public ServiceDefinition.METHOD getMethodType() {
            return methodType;
        }

        public String getPath() {
            return path;
        }

        public Class<?> getReturnClassType() {
            return method.getReturnType();
        }

        public Method getMethod() {
            return method;
        }

        public String getRootPath() {
            return rootPath;
        }
    }

    private static final class ServiceDefinitionInfo {

        private final ServiceDefinition serviceDefinition;
        private final Map<String, HttpMethodInfo> methodsMap;

        public ServiceDefinitionInfo(ServiceDefinition serviceDefinition, HashMap<String, HttpMethodInfo> methodsMap) {
            this.serviceDefinition = serviceDefinition;
            this.methodsMap = methodsMap;
        }

        public ServiceDefinition serviceDefinition() {
            return serviceDefinition;
        }

        public Map<String, HttpMethodInfo> methodsMap() {
            return methodsMap;
        }

    }

}
