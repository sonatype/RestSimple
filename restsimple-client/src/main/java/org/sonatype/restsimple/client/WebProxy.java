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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
 *
    public static interface ProxyClient {

        @GET
        @Path("getPet")
        @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        public Pet get(@PathParam("myPet") String path);

        @GET
        @Path("getPetString")
        @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        public String getString(@PathParam("myPet") String path);

        @POST
        @Path("addPet")
        @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        public Pet post(@PathParam("myPet") String myPet, String body);

        @DELETE
        @Path("deletePet")
        @Produces(PetstoreAction.APPLICATION + "/" + PetstoreAction.JSON)
        public Pet delete(@PathParam("myPet") String path);

    }

    ProxyClient client = WebProxy.createProxy(ProxyClient.class, URI.create("http://someurl/));
    Pet pet = client.post("myPet", "{\"name\":\"pouetpouet\"}");

 }
 *
 *
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
            ServiceDefinition.METHOD m = inf.getMethod();
            String path = inf.getPath();
            if (m == null) {
                throw new IllegalStateException(String.format("Unable to proxy method %s", method.getName()));
            }

            String uriString = uri.toURL().toString();
            StringBuilder builder = new StringBuilder(uriString);
            if (!uriString.endsWith("/") && path != null) {
                builder.append("/");
            }

            if (path != null) {
                if (path.startsWith("/")) {
                    builder.append(path.substring(1));
                } else {
                    builder.append(path);
                }
            }
            /**
             * TODO: quite dangerous, need to support T for body
             * TODO: The last element is always the body
             */
            String body = args[0].toString();
            if (args.length > 1) {
                body = args[args.length - 1].toString();
            }

            switch (m) {
                case GET:
                    return web.clientOf(builder.toString()).get(inf.getReturnClassType());
                case POST:
                    return web.clientOf(builder.toString()).post(body, inf.getReturnClassType());
                case DELETE:
                    return web.clientOf(builder.toString()).delete(body, inf.getReturnClassType());
                case PUT:
                    return web.clientOf(builder.toString()).put(body, inf.getReturnClassType());
                default:
                    throw new IllegalStateException(String.format("Invalid Method type %s", m));
            }
        }
    }

    private static ServiceDefinitionInfo createServiceDefinitionInfo(Class<?> clazz) {
        ServiceDefinition sd = new DefaultServiceDefinition();
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
                    httpMethodInfo = new HttpMethodInfo(ServiceDefinition.METHOD.GET, pathValue, m.getReturnType());
                    found = true;
                } else if (POST.class.isAssignableFrom(a.getClass())) {
                    sh = new PostServiceHandler(pathValue, new DummyAction());
                    httpMethodInfo = new HttpMethodInfo(ServiceDefinition.METHOD.POST, pathValue, m.getReturnType());
                    found = true;
                } else if (PUT.class.isAssignableFrom(a.getClass())) {
                    sh = new PutServiceHandler(pathValue, new DummyAction());
                    httpMethodInfo = new HttpMethodInfo(ServiceDefinition.METHOD.PUT, pathValue, m.getReturnType());
                    found = true;
                } else if (DELETE.class.isAssignableFrom(a.getClass())) {
                    sh = new DeleteServiceHandler(pathValue, new DummyAction());
                    httpMethodInfo = new HttpMethodInfo(ServiceDefinition.METHOD.DELETE, pathValue, m.getReturnType());
                    found = true;
                }

                if (found) {
                    sd.withHandler(sh);

                    Produces produces = m.getAnnotation(Produces.class);
                    if (produces != null) {
                        for (String p : produces.value()) {
                            logger.debug("Processing @Produces {}", p);
                            sh.producing(new MediaType(getType(p),getSubType(p)));
                        }
                    }

                    Consumes consumes = m.getAnnotation(Consumes.class);
                    if (consumes != null) {
                        for (String c : consumes.value()) {
                            // TODO
                            logger.debug("Processing @Consumes {}", c);
                            sh.consumeWith(new MediaType(getType(c),getSubType(c)), null);
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
                    pathBuilder.append("/").append(PathParam.class.cast(a).value());
                }
            }
        }
        return pathBuilder.toString();
    }

    private static final String getType(String mediaType) {
        int index = mediaType.indexOf("/");
        if (index < 1){
            return mediaType;
        }
        return mediaType.substring(0,index);
    }

    private static final String getSubType(String mediaType) {
        int index = mediaType.indexOf("/");
        if (index < 1){
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

        private final ServiceDefinition.METHOD method;
        private final String path;
        private final Class<?> returnClassType;

        public HttpMethodInfo(ServiceDefinition.METHOD method, String path, Class<?> returnClassType) {
            this.method = method;
            this.path = path;
            this.returnClassType = returnClassType;
        }

        public ServiceDefinition.METHOD getMethod() {
            return method;
        }

        public String getPath() {
            return path;
        }
        
        public Class<?> getReturnClassType() {
            return returnClassType;
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
