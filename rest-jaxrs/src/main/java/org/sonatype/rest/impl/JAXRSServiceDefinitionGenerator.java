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
package org.sonatype.rest.impl;

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
import org.sonatype.rest.api.MediaType;
import org.sonatype.rest.api.ResourceModuleConfig;
import org.sonatype.rest.api.ServiceDefinition;
import org.sonatype.rest.api.ServiceHandler;
import org.sonatype.rest.spi.ServiceDefinitionGenerator;

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
        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0;

        cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, "org/sonatype/rest/model/ServiceDescriptionResource", null, "java/lang/Object", null);

        {
            av0 = cw.visitAnnotation("Ljavax/ws/rs/Path;", true);
            av0.visit("value", serviceDefinition.path() + "/{method}/{id}/");
            av0.visitEnd();
        }
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
        cw.visitInnerClass("javax/ws/rs/core/Response$ResponseBuilder", "javax/ws/rs/core/Response", "ResponseBuilder", ACC_PUBLIC + ACC_STATIC + ACC_ABSTRACT);

        cw.visitInnerClass("javax/ws/rs/core/Response$Status", "javax/ws/rs/core/Response", "Status", ACC_PUBLIC + ACC_FINAL + ACC_STATIC + ACC_ENUM);

        cw.visitInnerClass("org/sonatype/rest/api/ServiceDefinition$HttpMethod", "org/sonatype/rest/api/ServiceDefinition", "HttpMethod", ACC_PUBLIC + ACC_FINAL + ACC_STATIC + ACC_ENUM);

        {
            fv = cw.visitField(ACC_PRIVATE, "logger", "Lorg/slf4j/Logger;", null, null);
            fv.visitEnd();
        }
        {
            fv = cw.visitField(0, "serviceEntity", "Lorg/sonatype/rest/api/ServiceEntity;", null, null);
            {
                av0 = fv.visitAnnotation("Lcom/google/inject/Inject;", true);
                av0.visitEnd();
            }
            fv.visitEnd();
        }
        {
            fv = cw.visitField(0, "mapper", "Lorg/sonatype/rest/spi/ServiceHandlerMapper;", null, null);
            {
                av0 = fv.visitAnnotation("Lcom/google/inject/Inject;", true);
                av0.visitEnd();
            }
            fv.visitEnd();
        }
        {
            fv = cw.visitField(0, "producer", "Lorg/sonatype/rest/api/ServiceHandlerMediaType;", null, null);
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
            mv.visitLdcInsn(Type.getType("Lorg/sonatype/rest/model/ServiceDescriptionResource;"));
            mv.visitMethodInsn(INVOKESTATIC, "org/slf4j/LoggerFactory", "getLogger", "(Ljava/lang/Class;)Lorg/slf4j/Logger;");
            mv.visitFieldInsn(PUTFIELD, "org/sonatype/rest/model/ServiceDescriptionResource", "logger", "Lorg/slf4j/Logger;");
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
                    mv = cw.visitMethod(ACC_PUBLIC, methodName, "(Ljava/lang/String;Ljava/lang/String;)Lorg/sonatype/rest/api/ServiceHandlerMediaType;", null, null);
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
                    mv.visitCode();
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, "org/sonatype/rest/model/ServiceDescriptionResource", "logger", "Lorg/slf4j/Logger;");
                    mv.visitLdcInsn("HTTP GET: Generated Resource invocation for method {} with id {}");
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "debug", "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V");
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitLdcInsn("get");
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitInsn(ACONST_NULL);
                    mv.visitMethodInsn(INVOKESPECIAL, "org/sonatype/rest/model/ServiceDescriptionResource", "createResponse", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");
                    mv.visitVarInsn(ASTORE, 3);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, "org/sonatype/rest/model/ServiceDescriptionResource", "producer", "Lorg/sonatype/rest/api/ServiceHandlerMediaType;");
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitMethodInsn(INVOKEINTERFACE, "org/sonatype/rest/api/ServiceHandlerMediaType", "visit", "(Ljava/lang/Object;)Lorg/sonatype/rest/api/ServiceHandlerMediaType;");
                    mv.visitInsn(ARETURN);
                    mv.visitMaxs(5, 4);
                    mv.visitEnd();
                }
            }
            {
                duplicateCounter = -1;
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
                        mv.visitCode();
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitFieldInsn(GETFIELD, "org/sonatype/rest/model/ServiceDescriptionResource", "logger", "Lorg/slf4j/Logger;");
                        mv.visitLdcInsn("HTTP HEAD: Generated Resource invocation for method {} with id {}");
                        mv.visitVarInsn(ALOAD, 1);
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "debug", "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V");
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitLdcInsn("head");
                        mv.visitVarInsn(ALOAD, 1);
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitInsn(ACONST_NULL);
                        mv.visitMethodInsn(INVOKESPECIAL, "org/sonatype/rest/model/ServiceDescriptionResource", "createResponse", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");
                        mv.visitVarInsn(ASTORE, 3);
                        mv.visitMethodInsn(INVOKESTATIC, "javax/ws/rs/core/Response", "ok", "()Ljavax/ws/rs/core/Response$ResponseBuilder;");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "javax/ws/rs/core/Response$ResponseBuilder", "build", "()Ljavax/ws/rs/core/Response;");
                        mv.visitInsn(ARETURN);
                        mv.visitMaxs(5, 4);
                        mv.visitEnd();
                    }
                }
            }
            {
                duplicateCounter = -1;
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
                        mv.visitCode();
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitFieldInsn(GETFIELD, "org/sonatype/rest/model/ServiceDescriptionResource", "logger", "Lorg/slf4j/Logger;");
                        mv.visitLdcInsn("HTTP PUT: Generated Resource invocation for method {} with id {}");
                        mv.visitVarInsn(ALOAD, 1);
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "debug", "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V");
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
                        mv.visitMethodInsn(INVOKESTATIC, "javax/ws/rs/core/UriBuilder", "fromResource", "(Ljava/lang/Class;)Ljavax/ws/rs/core/UriBuilder;");
                        mv.visitInsn(ICONST_2);
                        mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
                        mv.visitInsn(DUP);
                        mv.visitInsn(ICONST_0);
                        mv.visitLdcInsn("");
                        mv.visitInsn(AASTORE);
                        mv.visitInsn(DUP);
                        mv.visitInsn(ICONST_1);
                        mv.visitLdcInsn("");
                        mv.visitInsn(AASTORE);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "javax/ws/rs/core/UriBuilder", "build", "([Ljava/lang/Object;)Ljava/net/URI;");
                        mv.visitVarInsn(ASTORE, 3);
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitLdcInsn("put");
                        mv.visitVarInsn(ALOAD, 1);
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitInsn(ACONST_NULL);
                        mv.visitMethodInsn(INVOKESPECIAL, "org/sonatype/rest/model/ServiceDescriptionResource", "createResponse", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");
                        mv.visitVarInsn(ASTORE, 4);
                        mv.visitVarInsn(ALOAD, 3);
                        mv.visitMethodInsn(INVOKESTATIC, "javax/ws/rs/core/Response", "created", "(Ljava/net/URI;)Ljavax/ws/rs/core/Response$ResponseBuilder;");
                        mv.visitVarInsn(ALOAD, 4);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "javax/ws/rs/core/Response$ResponseBuilder", "entity", "(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "javax/ws/rs/core/Response$ResponseBuilder", "build", "()Ljavax/ws/rs/core/Response;");
                        mv.visitInsn(ARETURN);
                        mv.visitMaxs(5, 5);
                        mv.visitEnd();
                    }
                }
                {
                    duplicateCounter = -1;
                    if (serviceHandler.getHttpMethod().name().equalsIgnoreCase("post")) {
                        {
                            String methodName = serviceHandler.getHttpMethod().name().toLowerCase();
                            if (duplicateCounter++ >= 0) {
                                methodName = methodName + "_" + duplicateCounter;
                            }

                            mv = cw.visitMethod(ACC_PUBLIC, methodName, "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljavax/ws/rs/core/Response;", null, null);
                            {
                                av0 = mv.visitAnnotation("Ljavax/ws/rs/POST;", true);
                                av0.visitEnd();
                            }
                            {
                                av0 = mv.visitAnnotation("Ljavax/ws/rs/Consumes;", true);
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
                            {
                                av0 = mv.visitParameterAnnotation(2, "Ljavax/ws/rs/FormParam;", true);
                                av0.visit("value", "update");
                                av0.visitEnd();
                            }
                            mv.visitCode();
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitFieldInsn(GETFIELD, "org/sonatype/rest/model/ServiceDescriptionResource", "logger", "Lorg/slf4j/Logger;");
                            mv.visitLdcInsn("HTTP POST: Generated Resource invocation for method {} with id {} and update {}");
                            mv.visitVarInsn(ALOAD, 1);
                            mv.visitVarInsn(ALOAD, 2);
                            mv.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "debug", "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V");
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitLdcInsn("post");
                            mv.visitVarInsn(ALOAD, 1);
                            mv.visitVarInsn(ALOAD, 2);
                            mv.visitVarInsn(ALOAD, 3);
                            mv.visitMethodInsn(INVOKESPECIAL, "org/sonatype/rest/model/ServiceDescriptionResource", "createResponse", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");
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
                            mv.visitMaxs(5, 5);
                            mv.visitEnd();
                        }
                    }
                }
                {
                    duplicateCounter = -1;
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
                            mv.visitCode();
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitFieldInsn(GETFIELD, "org/sonatype/rest/model/ServiceDescriptionResource", "logger", "Lorg/slf4j/Logger;");
                            mv.visitLdcInsn("HTTP DELETE: Generated Resource invocation for method {} with id {}");
                            mv.visitVarInsn(ALOAD, 1);
                            mv.visitVarInsn(ALOAD, 2);
                            mv.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "debug", "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V");
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitLdcInsn("delete");
                            mv.visitVarInsn(ALOAD, 1);
                            mv.visitVarInsn(ALOAD, 2);
                            mv.visitInsn(ACONST_NULL);
                            mv.visitMethodInsn(INVOKESPECIAL, "org/sonatype/rest/model/ServiceDescriptionResource", "createResponse", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");
                            mv.visitVarInsn(ASTORE, 3);
                            mv.visitVarInsn(ALOAD, 3);
                            mv.visitMethodInsn(INVOKESTATIC, "javax/ws/rs/core/Response", "ok", "(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;");
                            mv.visitMethodInsn(INVOKEVIRTUAL, "javax/ws/rs/core/Response$ResponseBuilder", "build", "()Ljavax/ws/rs/core/Response;");
                            mv.visitInsn(ARETURN);
                            mv.visitMaxs(5, 4);
                            mv.visitEnd();
                        }
                    }
                }
            }
        }
        {
            mv = cw.visitMethod(ACC_PRIVATE, "createResponse", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;", null, null);
            mv.visitCode();
            Label l0 = new Label();
            Label l1 = new Label();
            Label l2 = new Label();
            mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Throwable");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, "org/sonatype/rest/model/ServiceDescriptionResource", "mapper", "Lorg/sonatype/rest/spi/ServiceHandlerMapper;");
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/sonatype/rest/spi/ServiceHandlerMapper", "map", "(Ljava/lang/String;)Lorg/sonatype/rest/api/ServiceHandler;");
            mv.visitVarInsn(ASTORE, 5);
            mv.visitVarInsn(ALOAD, 5);
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
            mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"org/sonatype/rest/api/ServiceHandler"}, 0, null);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/sonatype/rest/api/ServiceHandler", "getHttpMethod", "()Lorg/sonatype/rest/api/ServiceDefinition$HttpMethod;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/sonatype/rest/api/ServiceDefinition$HttpMethod", "name", "()Ljava/lang/String;");
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
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/sonatype/rest/api/ServiceHandler", "getMethod", "()Ljava/lang/String;");
            mv.visitVarInsn(ASTORE, 6);
            mv.visitLabel(l0);
            mv.visitInsn(ACONST_NULL);
            mv.visitVarInsn(ASTORE, 7);
            mv.visitVarInsn(ALOAD, 4);
            Label l5 = new Label();
            mv.visitJumpInsn(IFNULL, l5);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, "org/sonatype/rest/model/ServiceDescriptionResource", "serviceEntity", "Lorg/sonatype/rest/api/ServiceEntity;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
            mv.visitVarInsn(ALOAD, 6);
            mv.visitInsn(ICONST_2);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
            mv.visitInsn(DUP);
            mv.visitInsn(ICONST_0);
            mv.visitLdcInsn(Type.getType("Ljava/lang/String;"));
            mv.visitInsn(AASTORE);
            mv.visitInsn(DUP);
            mv.visitInsn(ICONST_1);
            mv.visitLdcInsn(Type.getType("Ljava/lang/String;"));
            mv.visitInsn(AASTORE);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
            mv.visitVarInsn(ASTORE, 7);
            mv.visitVarInsn(ALOAD, 7);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, "org/sonatype/rest/model/ServiceDescriptionResource", "serviceEntity", "Lorg/sonatype/rest/api/ServiceEntity;");
            mv.visitInsn(ICONST_2);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
            mv.visitInsn(DUP);
            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitInsn(AASTORE);
            mv.visitInsn(DUP);
            mv.visitInsn(ICONST_1);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitInsn(AASTORE);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "java/lang/String");
            mv.visitVarInsn(ASTORE, 8);
            Label l6 = new Label();
            mv.visitJumpInsn(GOTO, l6);
            mv.visitLabel(l5);
            mv.visitFrame(Opcodes.F_APPEND, 2, new Object[]{"java/lang/String", "java/lang/reflect/Method"}, 0, null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, "org/sonatype/rest/model/ServiceDescriptionResource", "serviceEntity", "Lorg/sonatype/rest/api/ServiceEntity;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
            mv.visitVarInsn(ALOAD, 6);
            mv.visitInsn(ICONST_1);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
            mv.visitInsn(DUP);
            mv.visitInsn(ICONST_0);
            mv.visitLdcInsn(Type.getType("Ljava/lang/String;"));
            mv.visitInsn(AASTORE);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
            mv.visitVarInsn(ASTORE, 7);
            mv.visitVarInsn(ALOAD, 7);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, "org/sonatype/rest/model/ServiceDescriptionResource", "serviceEntity", "Lorg/sonatype/rest/api/ServiceEntity;");
            mv.visitInsn(ICONST_1);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
            mv.visitInsn(DUP);
            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitInsn(AASTORE);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitVarInsn(ASTORE, 8);
            mv.visitLabel(l6);
            mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/Object"}, 0, null);
            mv.visitVarInsn(ALOAD, 8);
            mv.visitLabel(l1);
            mv.visitInsn(ARETURN);
            mv.visitLabel(l2);
            mv.visitFrame(Opcodes.F_FULL, 7, new Object[]{"org/sonatype/rest/model/ServiceDescriptionResource", "java/lang/String", "java/lang/String", "java/lang/String", "java/lang/String", "org/sonatype/rest/api/ServiceHandler", "java/lang/String"}, 1, new Object[]{"java/lang/Throwable"});
            mv.visitVarInsn(ASTORE, 7);
            mv.visitTypeInsn(NEW, "javax/ws/rs/WebApplicationException");
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, 7);
            mv.visitMethodInsn(INVOKESPECIAL, "javax/ws/rs/WebApplicationException", "<init>", "(Ljava/lang/Throwable;)V");
            mv.visitInsn(ATHROW);
            mv.visitMaxs(6, 9);
            mv.visitEnd();
        }
        cw.visitEnd();

        byte[] bytes = cw.toByteArray();

        try {
            ClassLoader cl = new ByteClassloader(bytes, this.getClass().getClassLoader());
            Class<?> clazz = cl.loadClass("org.sonatype.rest.model.ServiceDescriptionResource");

            moduleConfig.bind(clazz);

        } catch (Throwable e) {
            logger.error("generate", e);
        }
    }

    private final static class ByteClassloader extends ClassLoader {

        private final byte[] clazzBytes;

        protected ByteClassloader(byte[] clazzBytes, ClassLoader parent) {
            super(parent);
            this.clazzBytes = clazzBytes;
        }

        protected Class findClass(String name) throws ClassNotFoundException {

            if (name.endsWith("ServiceDescriptionResource")) {
                return defineClass(name, clazzBytes, 0, clazzBytes.length);
            } else {
                return super.findClass(name);
            }
        }
    }

}




