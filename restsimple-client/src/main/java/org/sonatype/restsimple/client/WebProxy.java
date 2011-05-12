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

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.restsimple.annotation.Consumes;
import org.sonatype.restsimple.annotation.CookieParam;
import org.sonatype.restsimple.annotation.Delete;
import org.sonatype.restsimple.annotation.FormParam;
import org.sonatype.restsimple.annotation.Get;
import org.sonatype.restsimple.annotation.HeaderParam;
import org.sonatype.restsimple.annotation.MatrixParam;
import org.sonatype.restsimple.annotation.Path;
import org.sonatype.restsimple.annotation.PathParam;
import org.sonatype.restsimple.annotation.Post;
import org.sonatype.restsimple.annotation.Produces;
import org.sonatype.restsimple.annotation.Put;
import org.sonatype.restsimple.annotation.QueryParam;
import org.sonatype.restsimple.annotation.Timeout;
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
import org.sonatype.spice.jersey.client.ahc.config.DefaultAhcConfig;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple proxy that generates RestSimple client from an annotated interface.
 * As simple as {code
 * {@code
 * public static interface ProxyClient {
 *
 * @Get
 * @Path("getPet")
 * @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
 * public Pet get(@PathParam("myPet") String path);
 * @Get
 * @Path("getPetString")
 * @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
 * public String getString(@PathParam("myPet") String path);
 * @Post
 * @Path("addPet")
 * @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
 * public Pet post(@PathParam("myPet") String myPet, String body);
 * @Delete
 * @Path("deletePet")
 * @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
 * public Pet delete(@PathParam("myPet") String path);
 * <p/>
 * }
 * }
 * <p/>
 * }
 * <p/>
 * ProxyClient client = WebProxy.createProxy(ProxyClient.class, URI.create("http://someurl/));
 * Pet pet = client.post("myPet", "{\"name\":\"pouetpouet\"}");
 * <p/>
 * }
 */
public class WebProxy {

    /**
     * TODO: Refactor using the visitor pattern the annotation processing part..
     */

    public final static String SITEBRICKS_COMPAT = "sitebricks.quoteString";


    private final static Logger logger = LoggerFactory.getLogger(WebProxy.class);

    /**
     * Generate a HTTP client proxy based on an interface annotated with RestSimple annotations.
     *
     * @param clazz A class an interface annotated with RestSimple annotations.
     * @param uri   the based uri.
     * @param <T>
     * @return an instance of T
     */
    public static final <T> T createProxy(Class<T> clazz, URI uri) {
        return createProxy(clazz, uri, Collections.<String, String>emptyMap());
    }

    /**
     * Generate a HTTP client proxy based on an interface annotated with RestSimple annotations.
     *
     * @param clazz A class an interface annotated with RestSimple annotations.
     * @param uri   the based uri.
     * @param <T>
     * @return an instance of T
     */
    public static final <T> T createProxy(Class<T> clazz,
                                          URI uri,
                                          Map<String,String> bindings) {

        return createProxy(new ObjectMapper(), clazz, uri, bindings);
    }

    /**
     * Generate a HTTP client proxy based on an interface annotated with RestSimple annotations.
     *
     * @param clazz A class an interface annotated with RestSimple annotations.
     * @param uri   the based uri.
     * @param <T>
     * @return an instance of T
     */
    public static final <T> T createProxy(Class<T> clazz,
                                          URI uri,
                                          Map<String,String> bindings,
                                          Map<String,String> properties) {

        return createProxy(new ObjectMapper(), clazz, uri, bindings, properties);
    }
    /**
     * Generate a HTTP client proxy based on an interface annotated with RestSimple annotations.
     *
     * @param objectMapper The Jackson's {@link ObjectMapper}
     * @param clazz A class an interface annotated with RestSimple annotations.
     * @param uri   the based uri.
     * @param <T>
     * @return an instance of T
     */
    public static final <T> T createProxy(ObjectMapper objectMapper,
                                          Class<T> clazz,
                                          URI uri) {

        return createProxy(objectMapper, clazz, uri, Collections.<String, String>emptyMap());

    }

    /**
     * Generate a HTTP client proxy based on an interface annotated with RestSimple annotations.
     *
     * @param objectMapper The Jackson's {@link ObjectMapper}
     * @param clazz A class an interface annotated with RestSimple annotations.
     * @param uri   the based uri.
     * @param <T>
     * @return an instance of T
     */
    public static final <T> T createProxy(ObjectMapper objectMapper,
                                          Class<T> clazz,
                                          URI uri,
                                          Map<String,String> bindings) {

        return createProxy(objectMapper, clazz, uri, bindings, Collections.<String, String>emptyMap());
    }

    public static final <T> T createProxy(ObjectMapper objectMapper,
                                          Class<T> clazz, URI uri,
                                          Map<String,String> bindings,
                                          Map<String,String> properties ) {

        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
                new WebProxyHandler(uri, createServiceDefinition(clazz), clazz, objectMapper, bindings, properties));
    }

    private static class WebProxyHandler implements InvocationHandler {

        private final URI uri;
        private final WebClient webClient;
        private final Class<?> clazz;
        private final Map<String,String> bindings;
        private final Map<String,String> properties;

        private final ObjectMapper objectMapper;
        private final TypeFactory typeFactory = TypeFactory.defaultInstance();

        public WebProxyHandler(URI uri, ServiceDefinition serviceDefinition, Class<?> clazz) {
            this(uri, serviceDefinition, clazz, new ObjectMapper(), Collections.<String, String>emptyMap(), Collections.<String, String>emptyMap());
        }

        public WebProxyHandler(URI uri,
                               ServiceDefinition serviceDefinition,
                               Class<?> clazz,
                               ObjectMapper objectMapper,
                               Map<String,String> bindings,
                               Map<String,String> properties) {

            this.uri = uri;
            this.clazz = clazz;
            this.webClient = new WebAHCClient(serviceDefinition, (properties.get(SITEBRICKS_COMPAT) != null));
            this.objectMapper = objectMapper;
            this.bindings = bindings;
            this.properties = properties;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            // Stolen from Ali's Sitebricks implementation
            final StringBuilder rawUrl = new StringBuilder(uri.toURL().toString());
            final Path atClass = clazz.getAnnotation(Path.class);

            if (atClass != null) {
                rawUrl.append(atClass.value());
            } else {
                final Path atDeclaringClass = method.getDeclaringClass().getAnnotation(Path.class);
                if (atDeclaringClass != null) {
                    rawUrl.append(atDeclaringClass.value());
                }
            }

            final Path atMethod = method.getAnnotation(Path.class);
            if (atMethod != null) {
                rawUrl.append(atMethod.value());
            }

            if (rawUrl.toString().trim().length() == 0) {
                throw new IllegalStateException(String.format(
                        "Cannot calculate rest URL for [%s]. Is class and/or method annotated with @At?", method.getName()));
            }

            String resourcePath = constructPath(rawUrl.toString(),method, args);

            Object body = retrieveBody(method, args);

            Class<?> returnType = method.getReturnType();


            // TODO: Beurk...Sitebricks doesn't handle serialization/deserialization the same way as Jersey.
            boolean sbSupport = (String.class.isAssignableFrom(method.getReturnType())
                                    && properties.get(SITEBRICKS_COMPAT) != null);

            boolean deserializeLocally = false;
            if (sbSupport && Map.class.isAssignableFrom(returnType) || Collection.class.isAssignableFrom(returnType) || returnType == Object.class){
                returnType = String.class;
                deserializeLocally = true;
            }

            Object o = null;
            switch (methodType(method)) {
                case GET:
                    o = webClient.clientOf(resourcePath)
                            .headers(constructCookie(method, args, constructHeaders(method, args)))
                            .queryString(constructFormString(method, args, constructQueryString(method, args)))
                            .matrixParams(constructMatrix(method, args))
                            .get(returnType);
                    break;
                case POST:
                    o = webClient.clientOf(resourcePath)
                            .headers(constructCookie(method, args, constructHeaders(method, args)))
                            .queryString(constructFormString(method, args, constructQueryString(method, args)))
                            .matrixParams(constructMatrix(method, args))
                            .post(body, returnType);
                    break;
                case DELETE:
                    o = webClient.clientOf(resourcePath)
                            .headers(constructCookie(method, args, constructHeaders(method, args)))
                            .queryString(constructFormString(method, args, constructQueryString(method, args)))
                            .matrixParams(constructMatrix(method, args))
                            .delete(body, returnType);
                    break;
                case PUT:
                    o = webClient.clientOf(resourcePath)
                            .headers(constructCookie(method, args, constructHeaders(method, args)))
                            .queryString(constructFormString(method, args, constructQueryString(method, args)))
                            .matrixParams(constructMatrix(method, args))
                            .put(body, returnType);
                    break;
            }

            if (sbSupport || deserializeLocally){
                return validateType(o.toString() , method);
            } else {
                return o;
            }
        }

        private ServiceDefinition.METHOD methodType(Method method) {
            for (Annotation a: method.getAnnotations()) {
                if (Get.class.isAssignableFrom(a.getClass())) {
                   return ServiceDefinition.METHOD.GET;
                } else if (Post.class.isAssignableFrom(a.getClass())) {
                    return ServiceDefinition.METHOD.POST;
                } else if (Put.class.isAssignableFrom(a.getClass())) {
                    return ServiceDefinition.METHOD.PUT;
                } else if (Delete.class.isAssignableFrom(a.getClass())) {
                    return ServiceDefinition.METHOD.DELETE;
                }
            }
            throw new IllegalStateException("Method not supported");
        }

        private Object validateType(String responseBody, Method m) throws IOException {
            final Class<?> returnType = m.getReturnType();
            if (returnType != void.class) {
                final Type genericReturnType = m.getGenericReturnType();
                return objectMapper.readValue(responseBody, typeFactory.constructType(genericReturnType, clazz));
            }
            return null;
        }

        private String constructPath(String url, Method m, Object params[]) {
            StringBuilder pathBuilder = new StringBuilder();
            Annotation[][] ans = m.getParameterAnnotations();

            int position = 0;
            boolean added = false;
            for (Annotation[] annotations : ans) {
                for (Annotation a : annotations) {
                    if (PathParam.class.isAssignableFrom(a.getClass())) {
                        added = true;
                        String[] tokens = url.split("/");
                        String replace = params[position].toString();

                        // If the {} or : aren't specified, let's add it at the end.
                        boolean hackyTrick = true;
                        for (String s : tokens) {
                            if (s.startsWith("{") || s.startsWith(":")) {
                                // TODO: if the method types are not in the order this will fail.
                                // Try to use the global bindings
                                int end = s.startsWith( "{" ) ? s.length() - 1 : s.length();
                                s = s.substring(1, end);
                                s = bindings.get(s);

                                if (s == null && position + 1 > params.length) {
                                    throw new IllegalStateException("Missing {...} value");
                                } else if (s == null) {
                                    s = params[position++].toString();
                                }
                                hackyTrick = false;
                            }
                            pathBuilder.append(s).append("/");
                        }

                        if (hackyTrick) {
                            pathBuilder.append(replace);
                        } else {
                            pathBuilder.deleteCharAt(pathBuilder.length() - 1);
                        }
                    }
                }
                position++;
            }
            if (!added) {
                if (url.contains( ":" ) || url.contains( "{" )){
                    String[] tokens = url.split("/");
                    for (String s : tokens) {
                        if (s.startsWith("{") || s.startsWith(":")) {
                            int end = s.startsWith( "{" ) ? s.length() - 1 : s.length();
                            String newS = s.substring( 1, end );
                            String tmp = bindings.get( newS );
                            if ( tmp != null ) {
                                s = tmp;
                            }
                        }
                        pathBuilder.append(s).append("/");
                    }
                    pathBuilder.deleteCharAt(pathBuilder.length() - 1);
                } else {
                    pathBuilder.append(url);
                }
            }
            return pathBuilder.toString();
        }

        private Object retrieveBody(Method m, Object params[]) {
            Annotation[][] ans = m.getParameterAnnotations();

            int i = 0;
            for (Annotation[] annotations : ans) {
                if (annotations.length == 0) {
                    return params[i];
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

        private Map<String, String> constructMatrix(Method m, Object params[]) {
            Map<String, String> matrix = new HashMap<String, String>();
            Annotation[][] ans = m.getParameterAnnotations();

            int i = 0;
            for (Annotation[] annotations : ans) {
                for (Annotation a : annotations) {
                    if (MatrixParam.class.isAssignableFrom(a.getClass())) {
                        logger.debug("Processing @MatrixParam {}", a);
                        matrix.put(MatrixParam.class.cast(a).value(), params[i].toString());
                    }
                    i++;
                }
            }
            return matrix;
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

    private static void configureAhcConfig(Class<?> clazz, DefaultAhcConfig config) {
        Timeout timeout =  clazz.getAnnotation(Timeout.class);
        if (timeout != null) {
            config.getAsyncHttpClientConfigBuilder().setConnectionTimeoutInMs(timeout.connectTimeout() * 1000);
            config.getAsyncHttpClientConfigBuilder().setConnectionTimeoutInMs(timeout.connectTimeout() * 1000);
        }
    }

    private static ServiceDefinition createServiceDefinition(Class<?> clazz) {

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
        for (Method m : method) {
            logger.debug("Processing method {}", m);
            boolean found = false;
            for (Annotation a : m.getAnnotations()) {
                logger.debug("Processing annotation {}", a);

                Path path = m.getAnnotation(Path.class);
                String pathValue = "/";

                if (path != null) {
                    pathValue = path.value();
                }

                if (Get.class.isAssignableFrom(a.getClass())) {
                    sh = new GetServiceHandler(pathValue, new DummyAction());
                    found = true;
                } else if (Post.class.isAssignableFrom(a.getClass())) {
                    sh = new PostServiceHandler(pathValue, new DummyAction());
                    found = true;
                } else if (Put.class.isAssignableFrom(a.getClass())) {
                    sh = new PutServiceHandler(pathValue, new DummyAction());
                    found = true;
                } else if (Delete.class.isAssignableFrom(a.getClass())) {
                    sh = new DeleteServiceHandler(pathValue, new DummyAction());
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

                    break;
                }
            }
            if (!found) {
                throw new IllegalStateException("Method not supported:  " + m);
            }
        }
        return sd;
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

}
