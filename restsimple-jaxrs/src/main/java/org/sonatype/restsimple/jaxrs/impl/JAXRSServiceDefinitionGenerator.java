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
package org.sonatype.restsimple.jaxrs.impl;

import com.google.inject.Inject;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.restsimple.api.Action;
import org.sonatype.restsimple.api.MediaType;
import org.sonatype.restsimple.api.PostServiceHandler;
import org.sonatype.restsimple.api.ResourceModuleConfig;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.api.ServiceHandler;
import org.sonatype.restsimple.spi.ServiceDefinitionGenerator;

import java.util.List;
import java.util.UUID;

/**
 * Generate a JAXRS resource, and bind it.
 */
public class JAXRSServiceDefinitionGenerator implements ServiceDefinitionGenerator, Opcodes {

    private final ResourceModuleConfig moduleConfig;

    private final Logger logger = LoggerFactory.getLogger(JAXRSServiceDefinitionGenerator.class);

    @Inject
    public JAXRSServiceDefinitionGenerator(ResourceModuleConfig moduleConfig) {
        this.moduleConfig = moduleConfig;
    }

    @Override
    public void generate(ServiceDefinition serviceDefinition) {

        for (ServiceHandler serviceHandler : serviceDefinition.serviceHandlers()) {
            moduleConfig.bindToInstance(Action.class, serviceHandler.getAction());
        }

        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0;

        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        String className = "org/sonatype/restsimple/model/ServiceDescriptionResource" + uuid;

        cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);

        {
            av0 = cw.visitAnnotation("Ljavax/ws/rs/Path;", true);
            av0.visit("value", serviceDefinition.path() + "/{method}/{id}/");
            av0.visitEnd();
        }
        if (serviceDefinition.mediaToProduce().size() > 0) {
            {
                av0 = cw.visitAnnotation("Ljavax/ws/rs/Produces;", true);
                {
                    AnnotationVisitor av1 = av0.visitArray("value");
                    for (MediaType m : serviceDefinition.mediaToProduce()) {
                        av1.visit(null, m.toMediaType());
                    }
                    av1.visitEnd();
                }
                av0.visitEnd();
            }
        }
        if (serviceDefinition.mediaToConsume().size() > 0) {
            {
                av0 = cw.visitAnnotation("Ljavax/ws/rs/Consumes;", true);
                {
                    AnnotationVisitor av1 = av0.visitArray("value");
                    for (MediaType m : serviceDefinition.mediaToConsume()) {
                        av1.visit(null, m.toMediaType());
                    }
                    av1.visitEnd();
                }
                av0.visitEnd();
            }
        }
        cw.visitInnerClass("javax/ws/rs/core/Response$ResponseBuilder", "javax/ws/rs/core/Response", "ResponseBuilder", ACC_PUBLIC + ACC_STATIC + ACC_ABSTRACT);

        cw.visitInnerClass("javax/ws/rs/core/Response$Status", "javax/ws/rs/core/Response", "Status", ACC_PUBLIC + ACC_FINAL + ACC_STATIC + ACC_ENUM);

        cw.visitInnerClass("org/sonatype/restsimple/api/ServiceDefinition$HttpMethod", "org/sonatype/restsimple/api/ServiceDefinition", "HttpMethod", ACC_PUBLIC + ACC_FINAL + ACC_STATIC + ACC_ENUM);

        {
            fv = cw.visitField(ACC_PRIVATE, "logger", "Lorg/slf4j/Logger;", null, null);
            fv.visitEnd();
        }
        {
            fv = cw.visitField(0, "request", "Ljavax/servlet/http/HttpServletRequest;", null, null);
            {
                av0 = fv.visitAnnotation("Ljavax/ws/rs/core/Context;", true);
                av0.visitEnd();
            }
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
                    mv = cw.visitMethod(ACC_PUBLIC, methodName, "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;", null, null);

                    {
                        av0 = mv.visitAnnotation("Ljavax/ws/rs/GET;", true);
                        av0.visitEnd();
                    }

                    {
                        av0 = mv.visitParameterAnnotation(0, "Ljavax/ws/rs/PathParam;", true);
                        av0.visit("value", "method");
                        av0.visitEnd();
                    }
                    {
                        av0 = mv.visitParameterAnnotation(1, "Ljavax/ws/rs/PathParam;", true);
                        av0.visit("value", "id");
                        av0.visitEnd();
                    }
                    if (serviceHandler.mediaToProduce().size() > 0) {
                        {
                            av0 = mv.visitAnnotation("Ljavax/ws/rs/Produces;", true);
                            {
                                AnnotationVisitor av1 = av0.visitArray("value");
                                for (MediaType m : serviceHandler.mediaToProduce()) {
                                    av1.visit(null, m.toMediaType());
                                }
                                av1.visitEnd();
                            }
                            av0.visitEnd();
                        }
                    }
                    if (serviceHandler.consumeMediaType() != null) {
                        {
                            av0 = mv.visitAnnotation("Ljavax/ws/rs/Consumes;", true);
                            {
                                AnnotationVisitor av1 = av0.visitArray("value");
                                av1.visit(null, serviceHandler.consumeMediaType());
                                av1.visitEnd();
                            }
                            av0.visitEnd();
                        }
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
                    mv.visitInsn(ACONST_NULL);
                    mv.visitMethodInsn(INVOKESPECIAL, className, "invokeAction", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljavax/ws/rs/core/MultivaluedMap;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitVarInsn(ASTORE, 3);
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitInsn(ARETURN);
                    mv.visitMaxs(6, 4);
                    mv.visitEnd();
                    continue;
                }
            }
            {
                if (serviceHandler.getHttpMethod().name().equalsIgnoreCase("head")) {

                    {
                        String methodName = serviceHandler.getHttpMethod().name().toLowerCase();
                        if (duplicateCounter++ >= 0) {
                            methodName = methodName + "_" + duplicateCounter;
                        }

                        mv = cw.visitMethod(ACC_PUBLIC, methodName, "(Ljava/lang/String;Ljava/lang/String;)Ljavax/ws/rs/core/Response;", null, null);
                        {
                            av0 = mv.visitAnnotation("Ljavax/ws/rs/HEAD;", true);
                            av0.visitEnd();
                        }
                        {
                            av0 = mv.visitParameterAnnotation(0, "Ljavax/ws/rs/PathParam;", true);
                            av0.visit("value", "method");
                            av0.visitEnd();
                        }
                        {
                            av0 = mv.visitParameterAnnotation(1, "Ljavax/ws/rs/PathParam;", true);
                            av0.visit("value", "id");
                            av0.visitEnd();
                        }
                        if (serviceHandler.mediaToProduce().size() > 0) {
                            {
                                av0 = mv.visitAnnotation("Ljavax/ws/rs/Produces;", true);
                                {
                                    AnnotationVisitor av1 = av0.visitArray("value");
                                    for (MediaType m : serviceHandler.mediaToProduce()) {
                                        av1.visit(null, m.toMediaType());
                                    }
                                    av1.visitEnd();
                                }
                                av0.visitEnd();
                            }
                        }
                        if (serviceHandler.consumeMediaType() != null) {
                            {
                                av0 = mv.visitAnnotation("Ljavax/ws/rs/Consumes;", true);
                                {
                                    AnnotationVisitor av1 = av0.visitArray("value");
                                    av1.visit(null, serviceHandler.consumeMediaType());
                                    av1.visitEnd();
                                }
                                av0.visitEnd();
                            }
                        }
                        mv.visitCode();
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitFieldInsn(GETFIELD, className, "logger", "Lorg/slf4j/Logger;");
                        mv.visitLdcInsn("HTTP HEAD: Generated Resource invocation for method {} with id {}");
                        mv.visitVarInsn(ALOAD, 1);
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "debug", "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V");
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitLdcInsn("head");
                        mv.visitVarInsn(ALOAD, 1);
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitInsn(ACONST_NULL);
                        mv.visitInsn(ACONST_NULL);
                        mv.visitMethodInsn(INVOKESPECIAL, className, "invokeAction", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljavax/ws/rs/core/MultivaluedMap;Ljava/lang/Object;)Ljava/lang/Object;");
                        mv.visitVarInsn(ASTORE, 3);
                        mv.visitMethodInsn(INVOKESTATIC, "javax/ws/rs/core/Response", "ok", "()Ljavax/ws/rs/core/Response$ResponseBuilder;");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "javax/ws/rs/core/Response$ResponseBuilder", "build", "()Ljavax/ws/rs/core/Response;");
                        mv.visitInsn(ARETURN);
                        mv.visitMaxs(6, 4);
                        mv.visitEnd();
                        continue;
                    }
                }
            }
            {
                if (serviceHandler.getHttpMethod().name().equalsIgnoreCase("put")) {

                    {
                        String methodName = serviceHandler.getHttpMethod().name().toLowerCase();
                        if (duplicateCounter++ >= 0) {
                            methodName = methodName + "_" + duplicateCounter;
                        }

                        mv = cw.visitMethod(ACC_PUBLIC, methodName, "(Ljava/lang/String;Ljava/lang/String;)Ljavax/ws/rs/core/Response;", null, null);
                        {
                            av0 = mv.visitAnnotation("Ljavax/ws/rs/PUT;", true);
                            av0.visitEnd();
                        }
                        {
                            av0 = mv.visitParameterAnnotation(0, "Ljavax/ws/rs/PathParam;", true);
                            av0.visit("value", "method");
                            av0.visitEnd();
                        }
                        {
                            av0 = mv.visitParameterAnnotation(1, "Ljavax/ws/rs/PathParam;", true);
                            av0.visit("value", "id");
                            av0.visitEnd();
                        }
                        if (serviceHandler.mediaToProduce().size() > 0) {
                            {
                                av0 = mv.visitAnnotation("Ljavax/ws/rs/Produces;", true);
                                {
                                    AnnotationVisitor av1 = av0.visitArray("value");
                                    for (MediaType m : serviceHandler.mediaToProduce()) {
                                        av1.visit(null, m.toMediaType());
                                    }
                                    av1.visitEnd();
                                }
                                av0.visitEnd();
                            }
                        }
                        if (serviceHandler.consumeMediaType() != null) {
                            {
                                av0 = mv.visitAnnotation("Ljavax/ws/rs/Consumes;", true);
                                {
                                    AnnotationVisitor av1 = av0.visitArray("value");
                                    av1.visit(null, serviceHandler.consumeMediaType());
                                    av1.visitEnd();
                                }
                                av0.visitEnd();
                            }
                        }
                        mv.visitCode();
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitFieldInsn(GETFIELD, className, "logger", "Lorg/slf4j/Logger;");
                        mv.visitLdcInsn("HTTP PUT: Generated Resource invocation for method {} with id {}");
                        mv.visitVarInsn(ALOAD, 1);
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "debug", "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V");
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
                        mv.visitMethodInsn(INVOKESTATIC, "javax/ws/rs/core/UriBuilder", "fromResource", "(Ljava/lang/Class;)Ljavax/ws/rs/core/UriBuilder;");
                        mv.visitInsn(ICONST_3);
                        mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
                        mv.visitInsn(DUP);
                        mv.visitInsn(ICONST_0);
                        mv.visitLdcInsn("");
                        mv.visitInsn(AASTORE);
                        mv.visitInsn(DUP);
                        mv.visitInsn(ICONST_1);
                        mv.visitLdcInsn("");
                        mv.visitInsn(AASTORE);
                        mv.visitInsn(DUP);
                        mv.visitInsn(ICONST_2);
                        mv.visitLdcInsn("");
                        mv.visitInsn(AASTORE);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "javax/ws/rs/core/UriBuilder", "build", "([Ljava/lang/Object;)Ljava/net/URI;");
                        mv.visitVarInsn(ASTORE, 3);
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitLdcInsn("put");
                        mv.visitVarInsn(ALOAD, 1);
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitInsn(ACONST_NULL);
                        mv.visitInsn(ACONST_NULL);
                        mv.visitMethodInsn(INVOKESPECIAL, className, "invokeAction", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljavax/ws/rs/core/MultivaluedMap;Ljava/lang/Object;)Ljava/lang/Object;");
                        mv.visitVarInsn(ASTORE, 4);
                        mv.visitVarInsn(ALOAD, 3);
                        mv.visitMethodInsn(INVOKESTATIC, "javax/ws/rs/core/Response", "created", "(Ljava/net/URI;)Ljavax/ws/rs/core/Response$ResponseBuilder;");
                        mv.visitVarInsn(ALOAD, 4);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "javax/ws/rs/core/Response$ResponseBuilder", "entity", "(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "javax/ws/rs/core/Response$ResponseBuilder", "build", "()Ljavax/ws/rs/core/Response;");
                        mv.visitInsn(ARETURN);
                        mv.visitMaxs(6, 5);
                        mv.visitEnd();
                        continue;
                    }
                }
                {
                    if (serviceHandler.getHttpMethod().name().equalsIgnoreCase("post")) {

                        String methodName = serviceHandler.getHttpMethod().name().toLowerCase();
                        if (duplicateCounter++ >= 0) {
                            methodName = methodName + "_" + duplicateCounter;
                        }

                        List<String> formsParam = PostServiceHandler.class.cast(serviceHandler).formParams();
                        if (formsParam.size() > 0) {
                            {
                                mv = cw.visitMethod(ACC_PUBLIC, methodName, "(Ljava/lang/String;Ljava/lang/String;Ljavax/ws/rs/core/MultivaluedMap;)Ljavax/ws/rs/core/Response;", "(Ljava/lang/String;Ljava/lang/String;Ljavax/ws/rs/core/MultivaluedMap<Ljava/lang/String;Ljava/lang/String;>;)Ljavax/ws/rs/core/Response;", null);
                                {
                                    av0 = mv.visitAnnotation("Ljavax/ws/rs/POST;", true);
                                    av0.visitEnd();
                                }
                                {
                                    av0 = mv.visitAnnotation("Ljavax/ws/rs/Consumes;", true);
                                    {
                                        AnnotationVisitor av1 = av0.visitArray("value");
                                        av1.visit(null, "application/x-www-form-urlencoded");
                                        av1.visitEnd();
                                    }
                                    av0.visitEnd();
                                }
                                {
                                    av0 = mv.visitParameterAnnotation(0, "Ljavax/ws/rs/PathParam;", true);
                                    av0.visit("value", "method");
                                    av0.visitEnd();
                                }
                                {
                                    av0 = mv.visitParameterAnnotation(1, "Ljavax/ws/rs/PathParam;", true);
                                    av0.visit("value", "id");
                                    av0.visitEnd();
                                }
                                if (serviceHandler.mediaToProduce().size() > 0) {
                                    {
                                        av0 = mv.visitAnnotation("Ljavax/ws/rs/Produces;", true);
                                        {
                                            AnnotationVisitor av1 = av0.visitArray("value");
                                            for (MediaType m : serviceHandler.mediaToProduce()) {
                                                av1.visit(null, m.toMediaType());
                                            }
                                            av1.visitEnd();
                                        }
                                        av0.visitEnd();
                                    }
                                }
                                if (serviceHandler.consumeMediaType() != null) {
                                    {
                                        av0 = mv.visitAnnotation("Ljavax/ws/rs/Consumes;", true);
                                        {
                                            AnnotationVisitor av1 = av0.visitArray("value");
                                            av1.visit(null, serviceHandler.consumeMediaType());
                                            av1.visitEnd();
                                        }
                                        av0.visitEnd();
                                    }
                                }
                                mv.visitCode();
                                mv.visitVarInsn(ALOAD, 0);
                                mv.visitFieldInsn(GETFIELD, className, "logger", "Lorg/slf4j/Logger;");
                                mv.visitLdcInsn("HTTP POST: Generated Resource invocation for method {} with id {} and update {}");
                                mv.visitVarInsn(ALOAD, 1);
                                mv.visitVarInsn(ALOAD, 2);
                                mv.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "debug", "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V");
                                mv.visitVarInsn(ALOAD, 0);
                                mv.visitLdcInsn("post");
                                mv.visitVarInsn(ALOAD, 1);
                                mv.visitVarInsn(ALOAD, 2);
                                mv.visitVarInsn(ALOAD, 3);
                                mv.visitInsn(ACONST_NULL);
                                mv.visitMethodInsn(INVOKESPECIAL, className, "invokeAction", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljavax/ws/rs/core/MultivaluedMap;Ljava/lang/Object;)Ljava/lang/Object;");
                                mv.visitVarInsn(ASTORE, 4);
                                mv.visitVarInsn(ALOAD, 4);
                                Label l0 = new Label();
                                mv.visitJumpInsn(IFNONNULL, l0);
                                mv.visitFieldInsn(GETSTATIC, "javax/ws/rs/core/Response$Status", "NO_CONTENT", "Ljavax/ws/rs/core/Response$Status;");
                                mv.visitMethodInsn(INVOKESTATIC, "javax/ws/rs/core/Response", "status", "(Ljavax/ws/rs/core/Response$Status;)Ljavax/ws/rs/core/Response$ResponseBuilder;");
                                mv.visitMethodInsn(INVOKEVIRTUAL, "javax/ws/rs/core/Response$ResponseBuilder", "build", "()Ljavax/ws/rs/core/Response;");
                                mv.visitInsn(ARETURN);
                                mv.visitLabel(l0);
                                mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/Object"}, 0, null);
                                mv.visitVarInsn(ALOAD, 4);
                                mv.visitMethodInsn(INVOKESTATIC, "javax/ws/rs/core/Response", "ok", "(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;");
                                mv.visitMethodInsn(INVOKEVIRTUAL, "javax/ws/rs/core/Response$ResponseBuilder", "build", "()Ljavax/ws/rs/core/Response;");
                                mv.visitInsn(ARETURN);
                                mv.visitMaxs(6, 5);
                                mv.visitEnd();
                                continue;
                            }
                        } else {
                            mv = cw.visitMethod(ACC_PUBLIC, "postWithBody", "(Ljava/lang/String;Ljava/lang/String;)Ljavax/ws/rs/core/Response;", null, null);
                            {
                                av0 = mv.visitAnnotation("Ljavax/ws/rs/POST;", true);
                                av0.visitEnd();
                            }
                            {
                                av0 = mv.visitParameterAnnotation(0, "Ljavax/ws/rs/PathParam;", true);
                                av0.visit("value", "method");
                                av0.visitEnd();
                            }
                            {
                                av0 = mv.visitParameterAnnotation(1, "Ljavax/ws/rs/PathParam;", true);
                                av0.visit("value", "id");
                                av0.visitEnd();
                            }
                            if (serviceHandler.mediaToProduce().size() > 0) {
                                {
                                    av0 = mv.visitAnnotation("Ljavax/ws/rs/Produces;", true);
                                    {
                                        AnnotationVisitor av1 = av0.visitArray("value");
                                        for (MediaType m : serviceHandler.mediaToProduce()) {
                                            av1.visit(null, m.toMediaType());
                                        }
                                        av1.visitEnd();
                                    }
                                    av0.visitEnd();
                                }
                            }
                            if (serviceHandler.consumeMediaType() != null) {
                                {
                                    av0 = mv.visitAnnotation("Ljavax/ws/rs/Consumes;", true);
                                    {
                                        AnnotationVisitor av1 = av0.visitArray("value");
                                        av1.visit(null, serviceHandler.consumeMediaType());
                                        av1.visitEnd();
                                    }
                                    av0.visitEnd();
                                }
                            }
                            mv.visitCode();
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitFieldInsn(GETFIELD, className, "logger", "Lorg/slf4j/Logger;");
                            mv.visitLdcInsn("HTTP POST: Generated Resource invocation for method {} with id {} and update {}");
                            mv.visitVarInsn(ALOAD, 1);
                            mv.visitVarInsn(ALOAD, 2);
                            mv.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "debug", "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V");
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitLdcInsn("post");
                            mv.visitVarInsn(ALOAD, 1);
                            mv.visitVarInsn(ALOAD, 2);
                            mv.visitInsn(ACONST_NULL);
                            mv.visitInsn(ACONST_NULL);
                            mv.visitMethodInsn(INVOKESPECIAL, className, "invokeAction", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljavax/ws/rs/core/MultivaluedMap;Ljava/lang/Object;)Ljava/lang/Object;");
                            mv.visitVarInsn(ASTORE, 3);
                            mv.visitVarInsn(ALOAD, 3);
                            Label l0 = new Label();
                            mv.visitJumpInsn(IFNONNULL, l0);
                            mv.visitFieldInsn(GETSTATIC, "javax/ws/rs/core/Response$Status", "NO_CONTENT", "Ljavax/ws/rs/core/Response$Status;");
                            mv.visitMethodInsn(INVOKESTATIC, "javax/ws/rs/core/Response", "status", "(Ljavax/ws/rs/core/Response$Status;)Ljavax/ws/rs/core/Response$ResponseBuilder;");
                            mv.visitMethodInsn(INVOKEVIRTUAL, "javax/ws/rs/core/Response$ResponseBuilder", "build", "()Ljavax/ws/rs/core/Response;");
                            mv.visitInsn(ARETURN);
                            mv.visitLabel(l0);
                            mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/Object"}, 0, null);
                            mv.visitVarInsn(ALOAD, 3);
                            mv.visitMethodInsn(INVOKESTATIC, "javax/ws/rs/core/Response", "ok", "(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;");
                            mv.visitMethodInsn(INVOKEVIRTUAL, "javax/ws/rs/core/Response$ResponseBuilder", "build", "()Ljavax/ws/rs/core/Response;");
                            mv.visitInsn(ARETURN);
                            mv.visitMaxs(6, 4);
                            mv.visitEnd();
                            continue;
                        }
                    }
                }
                {
                    if (serviceHandler.getHttpMethod().name().equalsIgnoreCase("delete")) {
                        {
                            String methodName = serviceHandler.getHttpMethod().name().toLowerCase();
                            if (duplicateCounter++ >= 0) {
                                methodName = methodName + "_" + duplicateCounter;
                            }

                            mv = cw.visitMethod(ACC_PUBLIC, methodName, "(Ljava/lang/String;Ljava/lang/String;)Ljavax/ws/rs/core/Response;", null, null);
                            {
                                av0 = mv.visitAnnotation("Ljavax/ws/rs/DELETE;", true);
                                av0.visitEnd();
                            }
                            {
                                av0 = mv.visitParameterAnnotation(0, "Ljavax/ws/rs/PathParam;", true);
                                av0.visit("value", "method");
                                av0.visitEnd();
                            }
                            {
                                av0 = mv.visitParameterAnnotation(1, "Ljavax/ws/rs/PathParam;", true);
                                av0.visit("value", "id");
                                av0.visitEnd();
                            }
                            if (serviceHandler.mediaToProduce().size() > 0) {
                                {
                                    av0 = mv.visitAnnotation("Ljavax/ws/rs/Produces;", true);
                                    {
                                        AnnotationVisitor av1 = av0.visitArray("value");
                                        for (MediaType m : serviceHandler.mediaToProduce()) {
                                            av1.visit(null, m.toMediaType());
                                        }
                                        av1.visitEnd();
                                    }
                                    av0.visitEnd();
                                }
                            }
                            if (serviceHandler.consumeMediaType() != null) {
                                {
                                    av0 = mv.visitAnnotation("Ljavax/ws/rs/Consumes;", true);
                                    {
                                        AnnotationVisitor av1 = av0.visitArray("value");
                                        av1.visit(null, serviceHandler.consumeMediaType());
                                        av1.visitEnd();
                                    }
                                    av0.visitEnd();
                                }
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
                            mv.visitInsn(ACONST_NULL);
                            mv.visitMethodInsn(INVOKESPECIAL, className, "invokeAction", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljavax/ws/rs/core/MultivaluedMap;Ljava/lang/Object;)Ljava/lang/Object;");
                            mv.visitVarInsn(ASTORE, 3);
                            mv.visitVarInsn(ALOAD, 3);
                            mv.visitMethodInsn(INVOKESTATIC, "javax/ws/rs/core/Response", "ok", "(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;");
                            mv.visitMethodInsn(INVOKEVIRTUAL, "javax/ws/rs/core/Response$ResponseBuilder", "build", "()Ljavax/ws/rs/core/Response;");
                            mv.visitInsn(ARETURN);
                            mv.visitMaxs(6, 4);
                            mv.visitEnd();
                            continue;
                        }
                    }
                }
            }
        }
        {
            mv = cw.visitMethod(ACC_PRIVATE, "invokeAction", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljavax/ws/rs/core/MultivaluedMap;Ljava/lang/Object;)Ljava/lang/Object;", "<T:Ljava/lang/Object;>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljavax/ws/rs/core/MultivaluedMap<Ljava/lang/String;Ljava/lang/String;>;TT;)Ljava/lang/Object;", null);
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
            mv.visitTypeInsn(NEW, "javax/ws/rs/WebApplicationException");
            mv.visitInsn(DUP);
            mv.visitTypeInsn(NEW, "java/lang/IllegalStateException");
            mv.visitInsn(DUP);
            mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V");
            mv.visitLdcInsn("No ServiceHandler defined for service ");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalStateException", "<init>", "(Ljava/lang/String;)V");
            mv.visitMethodInsn(INVOKESPECIAL, "javax/ws/rs/WebApplicationException", "<init>", "(Ljava/lang/Throwable;)V");
            mv.visitInsn(ATHROW);
            mv.visitLabel(l3);
            mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"org/sonatype/restsimple/api/ServiceHandler"}, 0, null);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/sonatype/restsimple/api/ServiceHandler", "getHttpMethod", "()Lorg/sonatype/restsimple/api/ServiceDefinition$METHOD;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/sonatype/restsimple/api/ServiceDefinition$METHOD", "name", "()Ljava/lang/String;");
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equalsIgnoreCase", "(Ljava/lang/String;)Z");
            Label l4 = new Label();
            mv.visitJumpInsn(IFNE, l4);
            mv.visitTypeInsn(NEW, "javax/ws/rs/WebApplicationException");
            mv.visitInsn(DUP);
            mv.visitIntInsn(SIPUSH, 405);
            mv.visitMethodInsn(INVOKESTATIC, "javax/ws/rs/core/Response", "status", "(I)Ljavax/ws/rs/core/Response$ResponseBuilder;");
            mv.visitLdcInsn("Method not allowed");
            mv.visitMethodInsn(INVOKEVIRTUAL, "javax/ws/rs/core/Response$ResponseBuilder", "entity", "(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "javax/ws/rs/core/Response$ResponseBuilder", "build", "()Ljavax/ws/rs/core/Response;");
            mv.visitMethodInsn(INVOKESPECIAL, "javax/ws/rs/WebApplicationException", "<init>", "(Ljavax/ws/rs/core/Response;)V");
            mv.visitInsn(ATHROW);
            mv.visitLabel(l4);
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
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, "request", "Ljavax/servlet/http/HttpServletRequest;");
            mv.visitMethodInsn(INVOKEINTERFACE, "javax/servlet/http/HttpServletRequest", "getMethod", "()Ljava/lang/String;");
            mv.visitMethodInsn(INVOKESPECIAL, className, "mapMethod", "(Ljava/lang/String;)Lorg/sonatype/restsimple/api/ServiceDefinition$METHOD;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, className, "mapHeaders", "()Ljava/util/Map;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKESPECIAL, className, "mapFormParams", "(Ljavax/ws/rs/core/MultivaluedMap;)Ljava/util/Map;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, "request", "Ljavax/servlet/http/HttpServletRequest;");
            mv.visitMethodInsn(INVOKEINTERFACE, "javax/servlet/http/HttpServletRequest", "getInputStream", "()Ljavax/servlet/ServletInputStream;");
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKESPECIAL, "org/sonatype/restsimple/api/ActionContext", "<init>", "(Lorg/sonatype/restsimple/api/ServiceDefinition$METHOD;Ljava/util/Map;Ljava/util/Map;Ljava/io/InputStream;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V");
            mv.visitVarInsn(ASTORE, 9);
            mv.visitVarInsn(ALOAD, 8);
            mv.visitVarInsn(ALOAD, 9);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/sonatype/restsimple/api/Action", "action", "(Lorg/sonatype/restsimple/api/ActionContext;)Ljava/lang/Object;");
            mv.visitVarInsn(ASTORE, 7);
            mv.visitLabel(l1);
            Label l5 = new Label();
            mv.visitJumpInsn(GOTO, l5);
            mv.visitLabel(l2);
            mv.visitFrame(Opcodes.F_FULL, 9, new Object[]{className, "java/lang/String", "java/lang/String", "java/lang/String", "javax/ws/rs/core/MultivaluedMap", "java/lang/Object", "org/sonatype/restsimple/api/ServiceHandler", "java/lang/Object", "org/sonatype/restsimple/api/Action"}, 1, new Object[]{"java/lang/Throwable"});
            mv.visitVarInsn(ASTORE, 9);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, "logger", "Lorg/slf4j/Logger;");
            mv.visitLdcInsn("invokeAction");
            mv.visitVarInsn(ALOAD, 9);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "error", "(Ljava/lang/String;Ljava/lang/Throwable;)V");
            mv.visitTypeInsn(NEW, "javax/ws/rs/WebApplicationException");
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, 9);
            mv.visitMethodInsn(INVOKESPECIAL, "javax/ws/rs/WebApplicationException", "<init>", "(Ljava/lang/Throwable;)V");
            mv.visitInsn(ATHROW);
            mv.visitLabel(l5);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 7);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(9, 10);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PRIVATE, "mapFormParams", "(Ljavax/ws/rs/core/MultivaluedMap;)Ljava/util/Map;", "(Ljavax/ws/rs/core/MultivaluedMap<Ljava/lang/String;Ljava/lang/String;>;)Ljava/util/Map<Ljava/lang/String;Ljava/util/Collection<Ljava/lang/String;>;>;", null);
            mv.visitCode();
            mv.visitTypeInsn(NEW, "java/util/HashMap");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V");
            mv.visitVarInsn(ASTORE, 2);
            mv.visitVarInsn(ALOAD, 1);
            Label l0 = new Label();
            mv.visitJumpInsn(IFNULL, l0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "javax/ws/rs/core/MultivaluedMap", "keySet", "()Ljava/util/Set;");
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "iterator", "()Ljava/util/Iterator;");
            mv.visitVarInsn(ASTORE, 3);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitFrame(Opcodes.F_APPEND, 2, new Object[]{"java/util/Map", "java/util/Iterator"}, 0, null);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z");
            mv.visitJumpInsn(IFEQ, l0);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "java/lang/String");
            mv.visitVarInsn(ASTORE, 4);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
            Label l2 = new Label();
            mv.visitJumpInsn(IFNULL, l2);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "java/util/Collection");
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEINTERFACE, "javax/ws/rs/core/MultivaluedMap", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "java/util/Collection");
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Collection", "addAll", "(Ljava/util/Collection;)Z");
            mv.visitInsn(POP);
            Label l3 = new Label();
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l2);
            mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/String"}, 0, null);
            mv.visitTypeInsn(NEW, "java/util/ArrayList");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V");
            mv.visitVarInsn(ASTORE, 5);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEINTERFACE, "javax/ws/rs/core/MultivaluedMap", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "java/util/Collection");
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "addAll", "(Ljava/util/Collection;)Z");
            mv.visitInsn(POP);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitInsn(POP);
            mv.visitLabel(l3);
            mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
            mv.visitJumpInsn(GOTO, l1);
            mv.visitLabel(l0);
            mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
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
            mv = cw.visitMethod(ACC_PRIVATE, "mapHeaders", "()Ljava/util/Map;", "()Ljava/util/Map<Ljava/lang/String;Ljava/util/Collection<Ljava/lang/String;>;>;", null);
            mv.visitCode();
            mv.visitTypeInsn(NEW, "java/util/HashMap");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V");
            mv.visitVarInsn(ASTORE, 1);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, "request", "Ljavax/servlet/http/HttpServletRequest;");
            mv.visitMethodInsn(INVOKEINTERFACE, "javax/servlet/http/HttpServletRequest", "getHeaderNames", "()Ljava/util/Enumeration;");
            mv.visitVarInsn(ASTORE, 2);
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitFrame(Opcodes.F_APPEND, 2, new Object[]{"java/util/Map", "java/util/Enumeration"}, 0, null);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Enumeration", "hasMoreElements", "()Z");
            Label l1 = new Label();
            mv.visitJumpInsn(IFEQ, l1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Enumeration", "nextElement", "()Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "java/lang/String");
            mv.visitVarInsn(ASTORE, 3);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
            Label l2 = new Label();
            mv.visitJumpInsn(IFNULL, l2);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "java/util/Collection");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, "request", "Ljavax/servlet/http/HttpServletRequest;");
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKEINTERFACE, "javax/servlet/http/HttpServletRequest", "getHeader", "(Ljava/lang/String;)Ljava/lang/String;");
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Collection", "add", "(Ljava/lang/Object;)Z");
            mv.visitInsn(POP);
            mv.visitJumpInsn(GOTO, l0);
            mv.visitLabel(l2);
            mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/String"}, 0, null);
            mv.visitTypeInsn(NEW, "java/util/ArrayList");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V");
            mv.visitVarInsn(ASTORE, 4);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, "request", "Ljavax/servlet/http/HttpServletRequest;");
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKEINTERFACE, "javax/servlet/http/HttpServletRequest", "getHeader", "(Ljava/lang/String;)Ljava/lang/String;");
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
            mv.visitInsn(POP);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitInsn(POP);
            mv.visitJumpInsn(GOTO, l0);
            mv.visitLabel(l1);
            mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESTATIC, "java/util/Collections", "unmodifiableMap", "(Ljava/util/Map;)Ljava/util/Map;");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(3, 5);
            mv.visitEnd();
        }
        cw.visitEnd();

        byte[] bytes = cw.toByteArray();

        try {
            String classToLoad = className.replace("/", ".");
            ClassLoader cl = new ByteClassloader(bytes, this.getClass().getClassLoader(), classToLoad);
            Class<?> clazz = cl.loadClass(classToLoad);

            moduleConfig.bind(clazz);
            moduleConfig.bind(GenericMessageBodyWriter.class);
        } catch (Throwable e) {
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
}







