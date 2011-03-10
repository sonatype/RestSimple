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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.restsimple.api.PostServiceHandler;
import org.sonatype.restsimple.api.ResourceModuleConfig;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.api.ServiceHandler;
import org.sonatype.restsimple.spi.ServiceDefinitionGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

/**
 * Generate a Sitebricks resource, and bind it.
 */
@Singleton
public class SitebricksServiceDefinitionGenerator implements ServiceDefinitionGenerator, Opcodes {

    private final ResourceModuleConfig moduleConfig;

    private final Logger logger = LoggerFactory.getLogger(SitebricksServiceDefinitionGenerator.class);

    private com.google.sitebricks.SitebricksModule module;

    @Inject
    public SitebricksServiceDefinitionGenerator(ResourceModuleConfig moduleConfig) {
        this.moduleConfig = moduleConfig;
    }

    private String convert(String path) {
        StringTokenizer st = new StringTokenizer(path, "/");
        StringBuilder newPath = new StringBuilder();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.startsWith("{")) {
                newPath.append("/").append(token.replace("{", ":").substring(0,newPath.length() -1));
            } else {
               newPath.append("/").append(token);
            }
        }
        return newPath.toString();
    }

    @Override
    public void generate(final ServiceDefinition serviceDefinition) {
        List<PostServiceHandler> postServiceHandlers = lookupPostServiceHandler(serviceDefinition);

        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0;

        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        String className = "org/sonatype/restsimple/model/SitebricksResource" + uuid;

        cw.visit(V1_6, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, className, null, "java/lang/Object", null);

        cw.visitInnerClass("org/sonatype/restsimple/api/ServiceDefinition$HttpMethod", "org/sonatype/restsimple/api/ServiceDefinition", "HttpMethod", ACC_PUBLIC + ACC_FINAL + ACC_STATIC + ACC_ENUM);

        if (postServiceHandlers.size() > 0) {
            for (PostServiceHandler postServiceHandler : postServiceHandlers) {
                for (String formParam : postServiceHandler.formParams()) {
                    {
                        fv = cw.visitField(ACC_PRIVATE, formParam, "Ljava/lang/String;", null, null);
                        fv.visitEnd();
                    }
                }
            }
        }
        {
            fv = cw.visitField(ACC_PRIVATE, "logger", "Lorg/slf4j/Logger;", null, null);
            fv.visitEnd();
        }
        {
            fv = cw.visitField(0, "mapper", "Lorg/sonatype/restsimple/spi/ServiceHandlerMapper;", null, null);
            {
                av0 = fv.visitAnnotation("Lcom/google/inject/Inject;", true);
                av0.visitEnd();
            }
            fv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitLdcInsn(Type.getType("L" + className + ";"));
            mv.visitMethodInsn(INVOKESTATIC, "org/slf4j/LoggerFactory", "getLogger", "(Ljava/lang/Class;)Lorg/slf4j/Logger;");
            mv.visitFieldInsn(PUTFIELD, className, "logger", "Lorg/slf4j/Logger;");
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 1);
            mv.visitEnd();
        }
        int duplicateCounter = -1;
        for (ServiceHandler serviceHandler : serviceDefinition.serviceHandlers()) {
            {
                String methodName = serviceHandler.getHttpMethod().name().toLowerCase();
                if (duplicateCounter++ >= 0) {
                    methodName = methodName + duplicateCounter;
                }

                if (serviceHandler.getHttpMethod().name().equalsIgnoreCase("get")) {

                    mv = cw.visitMethod(ACC_PUBLIC, methodName, "(Ljava/lang/String;Ljava/lang/String;Lcom/google/sitebricks/headless/Request;)Lcom/google/sitebricks/headless/Reply;", "(Ljava/lang/String;Ljava/lang/String;Lcom/google/sitebricks/headless/Request;)Lcom/google/sitebricks/headless/Reply<*>;", null);
                    {
                        av0 = mv.visitAnnotation("Lcom/google/sitebricks/http/Get;", true);
                        av0.visitEnd();
                    }
                    {
                        // TODO: Sitebricks only support one media
                        if (serviceDefinition.mediaToProduce().size() > 0) {
                            av0 = mv.visitAnnotation("Lcom/google/sitebricks/http/negotiate/Accept;", true);
                            av0.visit("value", serviceDefinition.mediaToProduce().get(0).toMediaType());
                            av0.visitEnd();
                        }
                    }
                    {
                        av0 = mv.visitParameterAnnotation(0, "Lcom/google/inject/name/Named;", true);
                        av0.visit("value", "method");
                        av0.visitEnd();
                    }
                    {
                        av0 = mv.visitParameterAnnotation(1, "Lcom/google/inject/name/Named;", true);
                        av0.visit("value", "id");
                        av0.visitEnd();
                    }
                    mv.visitCode();
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, className, "logger", "Lorg/slf4j/Logger;");
                    mv.visitLdcInsn("HTTP GET: Generated Resource invocation for method {} with id {}");
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "debug", "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V");
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitLdcInsn("get");
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitInsn(ACONST_NULL);
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitMethodInsn(INVOKESPECIAL, className, "createResponse", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;Lcom/google/sitebricks/headless/Request;)Ljava/lang/Object;");
                    mv.visitVarInsn(ASTORE, 4);
                    mv.visitLdcInsn(Type.getType("Lcom/google/sitebricks/headless/Reply;"));
                    mv.visitVarInsn(ALOAD, 4);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "isAssignableFrom", "(Ljava/lang/Class;)Z");
                    Label l0 = new Label();
                    mv.visitJumpInsn(IFEQ, l0);
                    mv.visitLdcInsn(Type.getType("Lcom/google/sitebricks/headless/Reply;"));
                    mv.visitVarInsn(ALOAD, 4);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "cast", "(Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitTypeInsn(CHECKCAST, "com/google/sitebricks/headless/Reply");
                    mv.visitInsn(ARETURN);
                    mv.visitLabel(l0);
                    mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/Object"}, 0, null);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitVarInsn(ALOAD, 4);
                    mv.visitMethodInsn(INVOKESPECIAL, className, "serializeResponse", "(Lcom/google/sitebricks/headless/Request;Ljava/lang/Object;)Lcom/google/sitebricks/headless/Reply;");
                    mv.visitInsn(ARETURN);
                    mv.visitMaxs(6, 5);
                    mv.visitEnd();
                    continue;
                }
            }
            if (serviceHandler.getHttpMethod().name().equalsIgnoreCase("put")) {

                {
                    String methodName = serviceHandler.getHttpMethod().name().toLowerCase();
                    if (duplicateCounter++ >= 0) {
                        methodName = methodName + "_" + duplicateCounter;
                    }

                    mv = cw.visitMethod(ACC_PUBLIC, methodName, "(Ljava/lang/String;Ljava/lang/String;Lcom/google/sitebricks/headless/Request;)Lcom/google/sitebricks/headless/Reply;", "(Ljava/lang/String;Ljava/lang/String;Lcom/google/sitebricks/headless/Request;)Lcom/google/sitebricks/headless/Reply<*>;", null);
                    {
                        av0 = mv.visitAnnotation("Lcom/google/sitebricks/http/Put;", true);
                        av0.visitEnd();
                    }
                    {
                        if (serviceDefinition.mediaToProduce().size() > 0) {
                            av0 = mv.visitAnnotation("Lcom/google/sitebricks/http/negotiate/Accept;", true);
                            av0.visit("value", serviceDefinition.mediaToProduce().get(0).toMediaType());
                            av0.visitEnd();
                        }
                    }
                    {
                        av0 = mv.visitParameterAnnotation(0, "Lcom/google/inject/name/Named;", true);
                        av0.visit("value", "method");
                        av0.visitEnd();
                    }
                    {
                        av0 = mv.visitParameterAnnotation(1, "Lcom/google/inject/name/Named;", true);
                        av0.visit("value", "id");
                        av0.visitEnd();
                    }
                    mv.visitCode();
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, className, "logger", "Lorg/slf4j/Logger;");
                    mv.visitLdcInsn("HTTP PUT: Generated Resource invocation for method {} with id {}");
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "debug", "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V");
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitLdcInsn("put");
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitInsn(ACONST_NULL);
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitMethodInsn(INVOKESPECIAL, className, "createResponse", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;Lcom/google/sitebricks/headless/Request;)Ljava/lang/Object;");
                    mv.visitVarInsn(ASTORE, 4);
                    mv.visitLdcInsn(Type.getType("Lcom/google/sitebricks/headless/Reply;"));
                    mv.visitVarInsn(ALOAD, 4);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "isAssignableFrom", "(Ljava/lang/Class;)Z");
                    Label l0 = new Label();
                    mv.visitJumpInsn(IFEQ, l0);
                    mv.visitLdcInsn(Type.getType("Lcom/google/sitebricks/headless/Reply;"));
                    mv.visitVarInsn(ALOAD, 4);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "cast", "(Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitTypeInsn(CHECKCAST, "com/google/sitebricks/headless/Reply");
                    mv.visitInsn(ARETURN);
                    mv.visitLabel(l0);
                    mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/Object"}, 0, null);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitVarInsn(ALOAD, 4);
                    mv.visitMethodInsn(INVOKESPECIAL, className, "serializeResponse", "(Lcom/google/sitebricks/headless/Request;Ljava/lang/Object;)Lcom/google/sitebricks/headless/Reply;");
                    mv.visitIntInsn(SIPUSH, 201);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "com/google/sitebricks/headless/Reply", "status", "(I)Lcom/google/sitebricks/headless/Reply;");
                    mv.visitInsn(ARETURN);
                    mv.visitMaxs(6, 5);
                    mv.visitEnd();
                    continue;
                }
            }

            if (serviceHandler.getHttpMethod().name().equalsIgnoreCase("post")) {
                {

                    String methodName = serviceHandler.getHttpMethod().name().toLowerCase();
                    if (duplicateCounter++ >= 0) {
                        methodName = methodName + "_" + duplicateCounter;
                    }


                    mv = cw.visitMethod(ACC_PUBLIC, methodName, "(Ljava/lang/String;Ljava/lang/String;Lcom/google/sitebricks/headless/Request;)Lcom/google/sitebricks/headless/Reply;", "(Ljava/lang/String;Ljava/lang/String;Lcom/google/sitebricks/headless/Request;)Lcom/google/sitebricks/headless/Reply<*>;", null);
                    {
                        av0 = mv.visitAnnotation("Lcom/google/sitebricks/http/Post;", true);
                        av0.visitEnd();
                    }
                    {
                        if (serviceDefinition.mediaToProduce().size() > 0) {
                            av0 = mv.visitAnnotation("Lcom/google/sitebricks/http/negotiate/Accept;", true);
                            av0.visit("value", serviceDefinition.mediaToProduce().get(0).toMediaType());
                            av0.visitEnd();
                        }
                    }
                    {
                        av0 = mv.visitParameterAnnotation(0, "Lcom/google/inject/name/Named;", true);
                        av0.visit("value", "method");
                        av0.visitEnd();
                    }
                    {
                        av0 = mv.visitParameterAnnotation(1, "Lcom/google/inject/name/Named;", true);
                        av0.visit("value", "id");
                        av0.visitEnd();
                    }
                    {
                        mv.visitCode();
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitFieldInsn(GETFIELD, className, "logger", "Lorg/slf4j/Logger;");
                        mv.visitLdcInsn("HTTP POST: Generated Resource invocation for method {} with id {} and update {}");
                        mv.visitVarInsn(ALOAD, 1);
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "debug", "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V");
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitFieldInsn(GETFIELD, className, "mapper", "Lorg/sonatype/restsimple/spi/ServiceHandlerMapper;");
                        mv.visitVarInsn(ALOAD, 1);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "org/sonatype/restsimple/spi/ServiceHandlerMapper", "map", "(Ljava/lang/String;)Lorg/sonatype/restsimple/api/ServiceHandler;");
                        mv.visitVarInsn(ASTORE, 4);
                        mv.visitLdcInsn(Type.getType("Lcom/google/sitebricks/client/transport/Text;"));
                        mv.visitVarInsn(ASTORE, 5);
                        mv.visitVarInsn(ALOAD, 4);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "org/sonatype/restsimple/api/ServiceHandler", "consumeMediaType", "()Lorg/sonatype/restsimple/api/MediaType;");
                        Label l0 = new Label();
                        mv.visitJumpInsn(IFNONNULL, l0);
                        mv.visitInsn(ACONST_NULL);
                        Label l1 = new Label();
                        mv.visitJumpInsn(GOTO, l1);
                        mv.visitLabel(l0);
                        mv.visitFrame(Opcodes.F_APPEND, 2, new Object[]{"org/sonatype/restsimple/api/ServiceHandler", "java/lang/Class"}, 0, null);
                        mv.visitVarInsn(ALOAD, 4);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "org/sonatype/restsimple/api/ServiceHandler", "consumeMediaType", "()Lorg/sonatype/restsimple/api/MediaType;");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "org/sonatype/restsimple/api/MediaType", "subType", "()Ljava/lang/String;");
                        mv.visitLabel(l1);
                        mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/String"});
                        mv.visitVarInsn(ASTORE, 6);
                        mv.visitVarInsn(ALOAD, 6);
                        Label l2 = new Label();
                        mv.visitJumpInsn(IFNULL, l2);
                        mv.visitVarInsn(ALOAD, 6);
                        mv.visitLdcInsn("json");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "endsWith", "(Ljava/lang/String;)Z");
                        mv.visitJumpInsn(IFEQ, l2);
                        mv.visitLdcInsn(Type.getType("Lcom/google/sitebricks/client/transport/Json;"));
                        mv.visitVarInsn(ASTORE, 5);
                        Label l3 = new Label();
                        mv.visitJumpInsn(GOTO, l3);
                        mv.visitLabel(l2);
                        mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/String"}, 0, null);
                        mv.visitVarInsn(ALOAD, 6);
                        mv.visitJumpInsn(IFNULL, l3);
                        mv.visitVarInsn(ALOAD, 6);
                        mv.visitLdcInsn("xml");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "endsWith", "(Ljava/lang/String;)Z");
                        mv.visitJumpInsn(IFEQ, l3);
                        mv.visitLdcInsn(Type.getType("Lcom/google/sitebricks/client/transport/Xml;"));
                        mv.visitVarInsn(ASTORE, 5);
                        mv.visitLabel(l3);
                        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                        mv.visitInsn(ACONST_NULL);
                        mv.visitVarInsn(ASTORE, 7);
                        mv.visitVarInsn(ALOAD, 4);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "org/sonatype/restsimple/api/ServiceHandler", "consumeClass", "()Ljava/lang/Class;");
                        Label l4 = new Label();
                        mv.visitJumpInsn(IFNULL, l4);
                        mv.visitVarInsn(ALOAD, 3);
                        mv.visitVarInsn(ALOAD, 4);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "org/sonatype/restsimple/api/ServiceHandler", "consumeClass", "()Ljava/lang/Class;");
                        mv.visitMethodInsn(INVOKEINTERFACE, "com/google/sitebricks/headless/Request", "read", "(Ljava/lang/Class;)Lcom/google/sitebricks/headless/Request$RequestRead;");
                        mv.visitVarInsn(ALOAD, 5);
                        mv.visitMethodInsn(INVOKEINTERFACE, "com/google/sitebricks/headless/Request$RequestRead", "as", "(Ljava/lang/Class;)Ljava/lang/Object;");
                        mv.visitVarInsn(ASTORE, 7);
                        mv.visitLabel(l4);
                        mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/Object"}, 0, null);
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitLdcInsn("post");
                        mv.visitVarInsn(ALOAD, 1);
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitVarInsn(ALOAD, 7);
                        mv.visitVarInsn(ALOAD, 3);
                        mv.visitMethodInsn(INVOKESPECIAL, className, "createResponse", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;Lcom/google/sitebricks/headless/Request;)Ljava/lang/Object;");
                        mv.visitVarInsn(ASTORE, 8);
                        mv.visitLdcInsn(Type.getType("Lcom/google/sitebricks/headless/Reply;"));
                        mv.visitVarInsn(ALOAD, 8);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "isAssignableFrom", "(Ljava/lang/Class;)Z");
                        Label l5 = new Label();
                        mv.visitJumpInsn(IFEQ, l5);
                        mv.visitLdcInsn(Type.getType("Lcom/google/sitebricks/headless/Reply;"));
                        mv.visitVarInsn(ALOAD, 8);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "cast", "(Ljava/lang/Object;)Ljava/lang/Object;");
                        mv.visitTypeInsn(CHECKCAST, "com/google/sitebricks/headless/Reply");
                        mv.visitInsn(ARETURN);
                        mv.visitLabel(l5);
                        mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/Object"}, 0, null);
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 3);
                        mv.visitVarInsn(ALOAD, 8);
                        mv.visitMethodInsn(INVOKESPECIAL, className, "serializeResponse", "(Lcom/google/sitebricks/headless/Request;Ljava/lang/Object;)Lcom/google/sitebricks/headless/Reply;");
                        mv.visitInsn(ARETURN);
                        mv.visitMaxs(6, 9);
                        mv.visitEnd();
                        continue;
                    }
                }
            }

            if (serviceHandler.getHttpMethod().name().equalsIgnoreCase("delete")) {
                {
                    String methodName = serviceHandler.getHttpMethod().name().toLowerCase();
                    if (duplicateCounter++ >= 0) {
                        methodName = methodName + "_" + duplicateCounter;
                    }

                    mv = cw.visitMethod(ACC_PUBLIC, methodName, "(Ljava/lang/String;Ljava/lang/String;Lcom/google/sitebricks/headless/Request;)Lcom/google/sitebricks/headless/Reply;", "(Ljava/lang/String;Ljava/lang/String;Lcom/google/sitebricks/headless/Request;)Lcom/google/sitebricks/headless/Reply<*>;", null);
                    {
                        av0 = mv.visitAnnotation("Lcom/google/sitebricks/http/Delete;", true);
                        av0.visitEnd();
                    }
                    {
                        if (serviceDefinition.mediaToProduce().size() > 0) {
                            av0 = mv.visitAnnotation("Lcom/google/sitebricks/http/negotiate/Accept;", true);
                            av0.visit("value", serviceDefinition.mediaToProduce().get(0).toMediaType());
                            av0.visitEnd();
                        }
                    }
                    {
                        av0 = mv.visitParameterAnnotation(0, "Lcom/google/inject/name/Named;", true);
                        av0.visit("value", "method");
                        av0.visitEnd();
                    }
                    {
                        av0 = mv.visitParameterAnnotation(1, "Lcom/google/inject/name/Named;", true);
                        av0.visit("value", "id");
                        av0.visitEnd();
                    }
                    mv.visitCode();
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, className, "logger", "Lorg/slf4j/Logger;");
                    mv.visitLdcInsn("HTTP DELETE: Generated Resource invocation for method {} with id {}");
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "debug", "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V");
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitLdcInsn("delete");
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitInsn(ACONST_NULL);
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitMethodInsn(INVOKESPECIAL, className, "createResponse", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;Lcom/google/sitebricks/headless/Request;)Ljava/lang/Object;");
                    mv.visitVarInsn(ASTORE, 4);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitVarInsn(ALOAD, 4);
                    mv.visitMethodInsn(INVOKESPECIAL, className, "serializeResponse", "(Lcom/google/sitebricks/headless/Request;Ljava/lang/Object;)Lcom/google/sitebricks/headless/Reply;");
                    mv.visitInsn(ARETURN);
                    mv.visitMaxs(6, 5);
                    mv.visitEnd();
                }
            }
        }

        {
            mv = cw.visitMethod(ACC_PRIVATE, "serializeResponse", "(Lcom/google/sitebricks/headless/Request;Ljava/lang/Object;)Lcom/google/sitebricks/headless/Reply;", "(Lcom/google/sitebricks/headless/Request;Ljava/lang/Object;)Lcom/google/sitebricks/headless/Reply<*>;", null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "com/google/sitebricks/headless/Request", "headers", "()Lcom/google/common/collect/Multimap;");
            mv.visitLdcInsn("Accept");
            mv.visitMethodInsn(INVOKEINTERFACE, "com/google/common/collect/Multimap", "get", "(Ljava/lang/Object;)Ljava/util/Collection;");
            mv.visitVarInsn(ASTORE, 3);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Collection", "size", "()I");
            Label l0 = new Label();
            mv.visitJumpInsn(IFLE, l0);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Collection", "iterator", "()Ljava/util/Iterator;");
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "java/lang/String");
            Label l1 = new Label();
            mv.visitJumpInsn(GOTO, l1);
            mv.visitLabel(l0);
            mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/util/Collection"}, 0, null);
            mv.visitInsn(ACONST_NULL);
            mv.visitLabel(l1);
            mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/String"});
            mv.visitVarInsn(ASTORE, 4);
            mv.visitVarInsn(ALOAD, 2);
            Label l2 = new Label();
            mv.visitJumpInsn(IFNONNULL, l2);
            mv.visitLdcInsn("");
            mv.visitMethodInsn(INVOKESTATIC, "com/google/sitebricks/headless/Reply", "with", "(Ljava/lang/Object;)Lcom/google/sitebricks/headless/Reply;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "com/google/sitebricks/headless/Reply", "noContent", "()Lcom/google/sitebricks/headless/Reply;");
            mv.visitInsn(ARETURN);
            mv.visitLabel(l2);
            mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/String"}, 0, null);
            mv.visitVarInsn(ALOAD, 4);
            Label l3 = new Label();
            mv.visitJumpInsn(IFNULL, l3);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitLdcInsn("json");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "endsWith", "(Ljava/lang/String;)Z");
            Label l4 = new Label();
            mv.visitJumpInsn(IFEQ, l4);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKESTATIC, "com/google/sitebricks/headless/Reply", "with", "(Ljava/lang/Object;)Lcom/google/sitebricks/headless/Reply;");
            mv.visitLdcInsn(Type.getType("Lcom/google/sitebricks/client/transport/Json;"));
            mv.visitMethodInsn(INVOKEVIRTUAL, "com/google/sitebricks/headless/Reply", "as", "(Ljava/lang/Class;)Lcom/google/sitebricks/headless/Reply;");
            mv.visitInsn(ARETURN);
            mv.visitLabel(l4);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitLdcInsn("xml");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "endsWith", "(Ljava/lang/String;)Z");
            mv.visitJumpInsn(IFEQ, l3);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKESTATIC, "com/google/sitebricks/headless/Reply", "with", "(Ljava/lang/Object;)Lcom/google/sitebricks/headless/Reply;");
            mv.visitLdcInsn(Type.getType("Lcom/google/sitebricks/client/transport/Xml;"));
            mv.visitMethodInsn(INVOKEVIRTUAL, "com/google/sitebricks/headless/Reply", "as", "(Ljava/lang/Class;)Lcom/google/sitebricks/headless/Reply;");
            mv.visitInsn(ARETURN);
            mv.visitLabel(l3);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;");
            mv.visitMethodInsn(INVOKESTATIC, "com/google/sitebricks/headless/Reply", "with", "(Ljava/lang/Object;)Lcom/google/sitebricks/headless/Reply;");
            mv.visitLdcInsn(Type.getType("Lcom/google/sitebricks/client/transport/Text;"));
            mv.visitMethodInsn(INVOKEVIRTUAL, "com/google/sitebricks/headless/Reply", "as", "(Ljava/lang/Class;)Lcom/google/sitebricks/headless/Reply;");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(2, 5);
            mv.visitEnd();
        }

        {
            mv = cw.visitMethod(ACC_PRIVATE, "createResponse", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;Lcom/google/sitebricks/headless/Request;)Ljava/lang/Object;", "<T:Ljava/lang/Object;>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;TT;Lcom/google/sitebricks/headless/Request;)Ljava/lang/Object;", null);
            mv.visitCode();
            Label l0 = new Label();
            Label l1 = new Label();
            Label l2 = new Label();
            mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Throwable");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, "mapper", "Lorg/sonatype/restsimple/spi/ServiceHandlerMapper;");
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/sonatype/restsimple/spi/ServiceHandlerMapper", "map", "(Ljava/lang/String;)Lorg/sonatype/restsimple/api/ServiceHandler;");
            mv.visitVarInsn(ASTORE, 6);
            mv.visitVarInsn(ALOAD, 6);
            Label l3 = new Label();
            mv.visitJumpInsn(IFNONNULL, l3);
            mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V");
            mv.visitLdcInsn("No ServiceHandler defined for service ");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
            mv.visitMethodInsn(INVOKESTATIC, "com/google/sitebricks/headless/Reply", "with", "(Ljava/lang/Object;)Lcom/google/sitebricks/headless/Reply;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "com/google/sitebricks/headless/Reply", "error", "()Lcom/google/sitebricks/headless/Reply;");
            mv.visitInsn(ARETURN);
            mv.visitLabel(l3);
            mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"org/sonatype/restsimple/api/ServiceHandler"}, 0, null);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/sonatype/restsimple/api/ServiceHandler", "getHttpMethod", "()Lorg/sonatype/restsimple/api/ServiceDefinition$METHOD;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/sonatype/restsimple/api/ServiceDefinition$METHOD", "name", "()Ljava/lang/String;");
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equalsIgnoreCase", "(Ljava/lang/String;)Z");
            Label l4 = new Label();
            mv.visitJumpInsn(IFNE, l4);
            mv.visitLdcInsn("Method not allowed");
            mv.visitMethodInsn(INVOKESTATIC, "com/google/sitebricks/headless/Reply", "with", "(Ljava/lang/Object;)Lcom/google/sitebricks/headless/Reply;");
            mv.visitIntInsn(SIPUSH, 405);
            mv.visitMethodInsn(INVOKEVIRTUAL, "com/google/sitebricks/headless/Reply", "status", "(I)Lcom/google/sitebricks/headless/Reply;");
            mv.visitInsn(ARETURN);
            mv.visitLabel(l4);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 4);
            Label l5 = new Label();
            mv.visitJumpInsn(IFNONNULL, l5);
            mv.visitLdcInsn("");
            mv.visitVarInsn(ASTORE, 4);
            mv.visitLabel(l5);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitInsn(ACONST_NULL);
            mv.visitVarInsn(ASTORE, 7);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/sonatype/restsimple/api/ServiceHandler", "getAction", "()Lorg/sonatype/restsimple/api/Action;");
            mv.visitVarInsn(ASTORE, 8);
            mv.visitLabel(l0);
            mv.visitTypeInsn(NEW, "org/sonatype/restsimple/api/ActionContext");
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESPECIAL, className, "mapMethod", "(Ljava/lang/String;)Lorg/sonatype/restsimple/api/ServiceDefinition$METHOD;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKEINTERFACE, "com/google/sitebricks/headless/Request", "headers", "()Lcom/google/common/collect/Multimap;");
            mv.visitMethodInsn(INVOKESPECIAL, className, "mapHeaders", "(Lcom/google/common/collect/Multimap;)Ljava/util/Map;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKEINTERFACE, "com/google/sitebricks/headless/Request", "params", "()Lcom/google/common/collect/Multimap;");
            mv.visitMethodInsn(INVOKESPECIAL, className, "mapFormParams", "(Lcom/google/common/collect/Multimap;)Ljava/util/Map;");
            mv.visitTypeInsn(NEW, "java/io/ByteArrayInputStream");
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "getBytes", "()[B");
            mv.visitMethodInsn(INVOKESPECIAL, "java/io/ByteArrayInputStream", "<init>", "([B)V");
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKESPECIAL, "org/sonatype/restsimple/api/ActionContext", "<init>", "(Lorg/sonatype/restsimple/api/ServiceDefinition$METHOD;Ljava/util/Map;Ljava/util/Map;Ljava/io/InputStream;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V");
            mv.visitVarInsn(ASTORE, 9);
            mv.visitVarInsn(ALOAD, 8);
            mv.visitVarInsn(ALOAD, 9);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/sonatype/restsimple/api/Action", "action", "(Lorg/sonatype/restsimple/api/ActionContext;)Ljava/lang/Object;");
            mv.visitVarInsn(ASTORE, 7);
            mv.visitLabel(l1);
            Label l6 = new Label();
            mv.visitJumpInsn(GOTO, l6);
            mv.visitLabel(l2);
            mv.visitFrame(Opcodes.F_FULL, 9, new Object[]{className, "java/lang/String", "java/lang/String", "java/lang/String", "java/lang/Object", "com/google/sitebricks/headless/Request", "org/sonatype/restsimple/api/ServiceHandler", "java/lang/Object", "org/sonatype/restsimple/api/Action"}, 1, new Object[]{"java/lang/Throwable"});
            mv.visitVarInsn(ASTORE, 9);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, "logger", "Lorg/slf4j/Logger;");
            mv.visitLdcInsn("delegate");
            mv.visitVarInsn(ALOAD, 9);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "error", "(Ljava/lang/String;Ljava/lang/Throwable;)V");
            mv.visitVarInsn(ALOAD, 9);
            mv.visitMethodInsn(INVOKESTATIC, "com/google/sitebricks/headless/Reply", "with", "(Ljava/lang/Object;)Lcom/google/sitebricks/headless/Reply;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "com/google/sitebricks/headless/Reply", "error", "()Lcom/google/sitebricks/headless/Reply;");
            mv.visitInsn(ARETURN);
            mv.visitLabel(l6);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 7);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(9, 10);
            mv.visitEnd();
        }

        {
            mv = cw.visitMethod(ACC_PRIVATE, "mapFormParams", "(Lcom/google/common/collect/Multimap;)Ljava/util/Map;", "(Lcom/google/common/collect/Multimap<Ljava/lang/String;Ljava/lang/String;>;)Ljava/util/Map<Ljava/lang/String;Ljava/util/Collection<Ljava/lang/String;>;>;", null);
            mv.visitCode();
            mv.visitTypeInsn(NEW, "java/util/HashMap");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V");
            mv.visitVarInsn(ASTORE, 2);
            mv.visitVarInsn(ALOAD, 1);
            Label l0 = new Label();
            mv.visitJumpInsn(IFNULL, l0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "com/google/common/collect/Multimap", "entries", "()Ljava/util/Collection;");
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Collection", "iterator", "()Ljava/util/Iterator;");
            mv.visitVarInsn(ASTORE, 3);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitFrame(Opcodes.F_APPEND, 2, new Object[]{"java/util/Map", "java/util/Iterator"}, 0, null);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z");
            Label l2 = new Label();
            mv.visitJumpInsn(IFEQ, l2);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "java/util/Map$Entry");
            mv.visitVarInsn(ASTORE, 4);
            mv.visitTypeInsn(NEW, "java/util/ArrayList");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V");
            mv.visitVarInsn(ASTORE, 5);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map$Entry", "getValue", "()Ljava/lang/Object;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)Z");
            mv.visitInsn(POP);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map$Entry", "getKey", "()Ljava/lang/Object;");
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitInsn(POP);
            mv.visitJumpInsn(GOTO, l1);
            mv.visitLabel(l2);
            mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKESTATIC, "java/util/Collections", "unmodifiableMap", "(Ljava/util/Map;)Ljava/util/Map;");
            mv.visitInsn(ARETURN);
            mv.visitLabel(l0);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(3, 6);
            mv.visitEnd();
        }

        {
            mv = cw.visitMethod(ACC_PRIVATE, "mapMethod", "(Ljava/lang/String;)Lorg/sonatype/restsimple/api/ServiceDefinition$METHOD;", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn("GET");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equalsIgnoreCase", "(Ljava/lang/String;)Z");
            Label l0 = new Label();
            mv.visitJumpInsn(IFEQ, l0);
            mv.visitFieldInsn(GETSTATIC, "org/sonatype/restsimple/api/ServiceDefinition$METHOD", "GET", "Lorg/sonatype/restsimple/api/ServiceDefinition$METHOD;");
            mv.visitInsn(ARETURN);
            mv.visitLabel(l0);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn("PUT");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equalsIgnoreCase", "(Ljava/lang/String;)Z");
            Label l1 = new Label();
            mv.visitJumpInsn(IFEQ, l1);
            mv.visitFieldInsn(GETSTATIC, "org/sonatype/restsimple/api/ServiceDefinition$METHOD", "PUT", "Lorg/sonatype/restsimple/api/ServiceDefinition$METHOD;");
            mv.visitInsn(ARETURN);
            mv.visitLabel(l1);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn("POST");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equalsIgnoreCase", "(Ljava/lang/String;)Z");
            Label l2 = new Label();
            mv.visitJumpInsn(IFEQ, l2);
            mv.visitFieldInsn(GETSTATIC, "org/sonatype/restsimple/api/ServiceDefinition$METHOD", "POST", "Lorg/sonatype/restsimple/api/ServiceDefinition$METHOD;");
            mv.visitInsn(ARETURN);
            mv.visitLabel(l2);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn("DELETE");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equalsIgnoreCase", "(Ljava/lang/String;)Z");
            Label l3 = new Label();
            mv.visitJumpInsn(IFEQ, l3);
            mv.visitFieldInsn(GETSTATIC, "org/sonatype/restsimple/api/ServiceDefinition$METHOD", "DELETE", "Lorg/sonatype/restsimple/api/ServiceDefinition$METHOD;");
            mv.visitInsn(ARETURN);
            mv.visitLabel(l3);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn("HEAD");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equalsIgnoreCase", "(Ljava/lang/String;)Z");
            Label l4 = new Label();
            mv.visitJumpInsn(IFEQ, l4);
            mv.visitFieldInsn(GETSTATIC, "org/sonatype/restsimple/api/ServiceDefinition$METHOD", "HEAD", "Lorg/sonatype/restsimple/api/ServiceDefinition$METHOD;");
            mv.visitInsn(ARETURN);
            mv.visitLabel(l4);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitTypeInsn(NEW, "java/lang/IllegalStateException");
            mv.visitInsn(DUP);
            mv.visitLdcInsn("Invalid Method");
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalStateException", "<init>", "(Ljava/lang/String;)V");
            mv.visitInsn(ATHROW);
            mv.visitMaxs(3, 2);
            mv.visitEnd();
        }

        {
            mv = cw.visitMethod(ACC_PRIVATE, "mapHeaders", "(Lcom/google/common/collect/Multimap;)Ljava/util/Map;", "(Lcom/google/common/collect/Multimap<Ljava/lang/String;Ljava/lang/String;>;)Ljava/util/Map<Ljava/lang/String;Ljava/util/Collection<Ljava/lang/String;>;>;", null);
            mv.visitCode();
            mv.visitTypeInsn(NEW, "java/util/HashMap");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V");
            mv.visitVarInsn(ASTORE, 2);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "com/google/common/collect/Multimap", "entries", "()Ljava/util/Collection;");
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Collection", "iterator", "()Ljava/util/Iterator;");
            mv.visitVarInsn(ASTORE, 3);
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitFrame(Opcodes.F_APPEND, 2, new Object[]{"java/util/Map", "java/util/Iterator"}, 0, null);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z");
            Label l1 = new Label();
            mv.visitJumpInsn(IFEQ, l1);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "java/util/Map$Entry");
            mv.visitVarInsn(ASTORE, 4);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map$Entry", "getKey", "()Ljava/lang/Object;");
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
            Label l2 = new Label();
            mv.visitJumpInsn(IFNULL, l2);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map$Entry", "getKey", "()Ljava/lang/Object;");
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "java/util/Collection");
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map$Entry", "getValue", "()Ljava/lang/Object;");
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Collection", "add", "(Ljava/lang/Object;)Z");
            mv.visitInsn(POP);
            Label l3 = new Label();
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l2);
            mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/util/Map$Entry"}, 0, null);
            mv.visitTypeInsn(NEW, "java/util/ArrayList");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V");
            mv.visitVarInsn(ASTORE, 5);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map$Entry", "getValue", "()Ljava/lang/Object;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)Z");
            mv.visitInsn(POP);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map$Entry", "getKey", "()Ljava/lang/Object;");
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitInsn(POP);
            mv.visitLabel(l3);
            mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
            mv.visitJumpInsn(GOTO, l0);
            mv.visitLabel(l1);
            mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKESTATIC, "java/util/Collections", "unmodifiableMap", "(Ljava/util/Map;)Ljava/util/Map;");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(3, 6);
            mv.visitEnd();
        }

        if (postServiceHandlers.size() > 0)

        {
            for (PostServiceHandler postServiceHandler : postServiceHandlers) {
                for (String formParam : postServiceHandler.formParams()) {
                    {
                        mv = cw.visitMethod(ACC_PUBLIC, "set" + formParam.substring(0, 1).toUpperCase() + formParam.substring(1), "(Ljava/lang/String;)V", null, null);
                        mv.visitCode();
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 1);
                        mv.visitFieldInsn(PUTFIELD, className, formParam, "Ljava/lang/String;");
                        mv.visitInsn(RETURN);
                        mv.visitMaxs(2, 2);
                        mv.visitEnd();
                    }
                }
            }
        }

        cw.visitEnd();

        byte[] bytes = cw.toByteArray();

        try {
            String classToLoad = className.replace("/", ".");
            ClassLoader cl = new ByteClassloader(bytes, this.getClass().getClassLoader(), classToLoad);
            final Class<?> clazz = cl.loadClass(classToLoad);
            final String path = serviceDefinition.path().contains("/{") ? convert(serviceDefinition.path()) : serviceDefinition.path();

            moduleConfig.bind(clazz);
            if (module == null) {
                module = new com.google.sitebricks.SitebricksModule() {
                    @Override
                    protected void configureSitebricks() {
                        at(path + "/:method/:id").serve(clazz);
                    }
                };
                moduleConfig.install(module);
            } else {
                module.at(path + "/:method/:id").serve(clazz);
            }

        }

        catch (Throwable e) {
            logger.error("generate", e);
        }
    }

    private final static class ByteClassloader extends ClassLoader {

        private final byte[] clazzBytes;
        private final String className;

        protected ByteClassloader(byte[] clazzBytes, ClassLoader parent, String className) {
            super(parent);
            this.clazzBytes = clazzBytes;
            this.className = className;
        }

        protected Class findClass(String name) throws ClassNotFoundException {

            if (name.compareTo(className) == 0) {
                return defineClass(name, clazzBytes, 0, clazzBytes.length);
            } else {
                return super.findClass(name);
            }
        }

    }

    private List<PostServiceHandler> lookupPostServiceHandler(ServiceDefinition serviceDefinition) {
        List<PostServiceHandler> l = new ArrayList<PostServiceHandler>();
        for (ServiceHandler s : serviceDefinition.serviceHandlers()) {
            if (s instanceof PostServiceHandler) {
                l.add((PostServiceHandler) s);
            }
        }
        return l;
    }

}




