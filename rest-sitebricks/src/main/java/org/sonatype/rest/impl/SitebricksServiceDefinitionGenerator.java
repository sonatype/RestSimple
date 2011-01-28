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
import org.sonatype.rest.api.PostServiceHandler;
import org.sonatype.rest.api.ResourceModuleConfig;
import org.sonatype.rest.api.ServiceDefinition;
import org.sonatype.rest.api.ServiceEntity;
import org.sonatype.rest.api.ServiceHandler;
import org.sonatype.rest.api.ServiceHandlerMediaType;
import org.sonatype.rest.spi.ServiceDefinitionGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Generate a Sitebricks resource, and bind it.
 */
public class SitebricksServiceDefinitionGenerator implements ServiceDefinitionGenerator, Opcodes {

    private final ResourceModuleConfig moduleConfig;

    private final Logger logger = LoggerFactory.getLogger(SitebricksServiceDefinitionGenerator.class);

    @Inject
    public SitebricksServiceDefinitionGenerator(ResourceModuleConfig moduleConfig) {
        this.moduleConfig = moduleConfig;
    }

    @Override
    public void generate(final ServiceDefinition serviceDefinition) {
        boolean isSet = false;
        for (ServiceHandler serviceHandler : serviceDefinition.serviceHandlers()) {
            if (serviceHandler.mediaType() != null) {
                moduleConfig.bindTo(ServiceHandlerMediaType.class, serviceHandler.mediaType());
                isSet = true;
            }
        }

        if (!isSet) {
            moduleConfig.bindTo(ServiceHandlerMediaType.class, StringServiceHandlerMediaType.class);
        }
        moduleConfig.bindToInstance(ServiceEntity.class, serviceDefinition.serviceEntity());

        List<PostServiceHandler> postServiceHandlers = lookupPostServiceHandler(serviceDefinition);

        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0;

        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        String className = "org/sonatype/rest/model/SitebricksResource" + uuid;

        cw.visit(V1_6, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, className, null, "java/lang/Object", null);

        cw.visitInnerClass("org/sonatype/rest/api/ServiceDefinition$HttpMethod", "org/sonatype/rest/api/ServiceDefinition", "HttpMethod", ACC_PUBLIC + ACC_FINAL + ACC_STATIC + ACC_ENUM);

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

                    mv = cw.visitMethod(ACC_PUBLIC, methodName, "(Ljava/lang/String;Ljava/lang/String;)Lcom/google/sitebricks/headless/Reply;", "(Ljava/lang/String;Ljava/lang/String;)Lcom/google/sitebricks/headless/Reply<*>;", null);
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
                    mv.visitMethodInsn(INVOKESPECIAL, className, "createResponse", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");
                    mv.visitVarInsn(ASTORE, 3);
                    mv.visitLdcInsn(Type.getType("Lcom/google/sitebricks/headless/Reply;"));
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "isAssignableFrom", "(Ljava/lang/Class;)Z");
                    Label l0 = new Label();
                    mv.visitJumpInsn(IFEQ, l0);
                    mv.visitLdcInsn(Type.getType("Lcom/google/sitebricks/headless/Reply;"));
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "cast", "(Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitTypeInsn(CHECKCAST, "com/google/sitebricks/headless/Reply");
                    mv.visitInsn(ARETURN);
                    mv.visitLabel(l0);
                    mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/Object"}, 0, null);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, className, "producer", "Lorg/sonatype/rest/api/ServiceHandlerMediaType;");
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitMethodInsn(INVOKEINTERFACE, "org/sonatype/rest/api/ServiceHandlerMediaType", "visit", "(Ljava/lang/Object;)Lorg/sonatype/rest/api/ServiceHandlerMediaType;");
                    mv.visitMethodInsn(INVOKESTATIC, "com/google/sitebricks/headless/Reply", "with", "(Ljava/lang/Object;)Lcom/google/sitebricks/headless/Reply;");
                    mv.visitLdcInsn(Type.getType("Lcom/google/sitebricks/client/transport/Json;"));
                    mv.visitMethodInsn(INVOKEVIRTUAL, "com/google/sitebricks/headless/Reply", "as", "(Ljava/lang/Class;)Lcom/google/sitebricks/headless/Reply;");
                    mv.visitInsn(ARETURN);
                    mv.visitMaxs(4, 4);
                    mv.visitEnd();

                }
            }
            duplicateCounter = -1;
            if (serviceHandler.getHttpMethod().name().equalsIgnoreCase("put")) {

                {
                    String methodName = serviceHandler.getHttpMethod().name().toLowerCase();
                    if (duplicateCounter++ >= 0) {
                        methodName = methodName + "_" + duplicateCounter;
                    }

                    mv = cw.visitMethod(ACC_PUBLIC, methodName, "(Ljava/lang/String;Ljava/lang/String;)Lcom/google/sitebricks/headless/Reply;", "(Ljava/lang/String;Ljava/lang/String;)Lcom/google/sitebricks/headless/Reply<*>;", null);
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
                    mv.visitMethodInsn(INVOKESPECIAL, className, "createResponse", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");
                    mv.visitVarInsn(ASTORE, 3);
                    mv.visitLdcInsn(Type.getType("Lcom/google/sitebricks/headless/Reply;"));
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "isAssignableFrom", "(Ljava/lang/Class;)Z");
                    Label l0 = new Label();
                    mv.visitJumpInsn(IFEQ, l0);
                    mv.visitLdcInsn(Type.getType("Lcom/google/sitebricks/headless/Reply;"));
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "cast", "(Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitTypeInsn(CHECKCAST, "com/google/sitebricks/headless/Reply");
                    mv.visitInsn(ARETURN);
                    mv.visitLabel(l0);
                    mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/Object"}, 0, null);
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;");
                    mv.visitMethodInsn(INVOKESTATIC, "com/google/sitebricks/headless/Reply", "with", "(Ljava/lang/Object;)Lcom/google/sitebricks/headless/Reply;");
                    mv.visitIntInsn(SIPUSH, 201);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "com/google/sitebricks/headless/Reply", "status", "(I)Lcom/google/sitebricks/headless/Reply;");
                    mv.visitInsn(ARETURN);
                    mv.visitMaxs(4, 4);
                    mv.visitEnd();
                }
            }

            duplicateCounter = -1;
            StringBuilder builder = new StringBuilder();
            if (serviceHandler.getHttpMethod().name().equalsIgnoreCase("post")) {
                {

                    String methodName = serviceHandler.getHttpMethod().name().toLowerCase();
                    if (duplicateCounter++ >= 0) {
                        methodName = methodName + "_" + duplicateCounter;
                    }

                    mv = cw.visitMethod(ACC_PUBLIC, methodName, "(Ljava/lang/String;Ljava/lang/String;)Lcom/google/sitebricks/headless/Reply;", "(Ljava/lang/String;Ljava/lang/String;)Lcom/google/sitebricks/headless/Reply<*>;", null);
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
                    mv.visitMethodInsn(INVOKESPECIAL, className, "createResponse", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");
                    mv.visitVarInsn(ASTORE, 3);
                    mv.visitLdcInsn(Type.getType("Lcom/google/sitebricks/headless/Reply;"));
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "isAssignableFrom", "(Ljava/lang/Class;)Z");
                    Label l0 = new Label();
                    mv.visitJumpInsn(IFEQ, l0);
                    mv.visitLdcInsn(Type.getType("Lcom/google/sitebricks/headless/Reply;"));
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "cast", "(Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitTypeInsn(CHECKCAST, "com/google/sitebricks/headless/Reply");
                    mv.visitInsn(ARETURN);
                    mv.visitLabel(l0);
                    mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/Object"}, 0, null);
                    mv.visitVarInsn(ALOAD, 3);
                    Label l1 = new Label();
                    mv.visitJumpInsn(IFNONNULL, l1);
                    mv.visitLdcInsn("");
                    mv.visitMethodInsn(INVOKESTATIC, "com/google/sitebricks/headless/Reply", "with", "(Ljava/lang/Object;)Lcom/google/sitebricks/headless/Reply;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "com/google/sitebricks/headless/Reply", "noContent", "()Lcom/google/sitebricks/headless/Reply;");
                    mv.visitInsn(ARETURN);
                    mv.visitLabel(l1);
                    mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;");
                    mv.visitMethodInsn(INVOKESTATIC, "com/google/sitebricks/headless/Reply", "with", "(Ljava/lang/Object;)Lcom/google/sitebricks/headless/Reply;");
                    mv.visitInsn(ARETURN);
                    mv.visitMaxs(4, 4);
                    mv.visitEnd();
                }
            }

            duplicateCounter = -1;
            if (serviceHandler.getHttpMethod().name().equalsIgnoreCase("delete")) {
                {
                    String methodName = serviceHandler.getHttpMethod().name().toLowerCase();
                    if (duplicateCounter++ >= 0) {
                        methodName = methodName + "_" + duplicateCounter;
                    }

                    mv = cw.visitMethod(ACC_PUBLIC, methodName, "(Ljava/lang/String;Ljava/lang/String;)Lcom/google/sitebricks/headless/Reply;", "(Ljava/lang/String;Ljava/lang/String;)Lcom/google/sitebricks/headless/Reply<*>;", null);
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
                    mv.visitMethodInsn(INVOKESPECIAL, className, "createResponse", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");
                    mv.visitVarInsn(ASTORE, 3);
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;");
                    mv.visitMethodInsn(INVOKESTATIC, "com/google/sitebricks/headless/Reply", "with", "(Ljava/lang/Object;)Lcom/google/sitebricks/headless/Reply;");
                    mv.visitInsn(ARETURN);
                    mv.visitMaxs(4, 4);
                    mv.visitEnd();
                }
            }
        }
        {
            mv = cw.visitMethod(ACC_PRIVATE, "createResponse", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;", null, null);
            mv.visitCode();
            Label l0 = new Label();
            Label l1 = new Label();
            Label l2 = new Label();
            mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Throwable");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, "mapper", "Lorg/sonatype/rest/spi/ServiceHandlerMapper;");
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/sonatype/rest/spi/ServiceHandlerMapper", "map", "(Ljava/lang/String;)Lorg/sonatype/rest/api/ServiceHandler;");
            mv.visitVarInsn(ASTORE, 4);
            mv.visitVarInsn(ALOAD, 4);
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
            mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"org/sonatype/rest/api/ServiceHandler"}, 0, null);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/sonatype/rest/api/ServiceHandler", "getHttpMethod", "()Lorg/sonatype/rest/api/ServiceDefinition$HttpMethod;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/sonatype/rest/api/ServiceDefinition$HttpMethod", "name", "()Ljava/lang/String;");
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
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/sonatype/rest/api/ServiceHandler", "getServiceEntityMethod", "()Ljava/lang/String;");
            mv.visitVarInsn(ASTORE, 5);
            mv.visitInsn(ICONST_1);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
            mv.visitVarInsn(ASTORE, 6);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitInsn(ICONST_0);
            mv.visitLdcInsn(Type.getType("Ljava/lang/String;"));
            mv.visitInsn(AASTORE);
            mv.visitInsn(ICONST_1);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            mv.visitVarInsn(ASTORE, 7);
            mv.visitVarInsn(ALOAD, 7);
            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitInsn(AASTORE);
            mv.visitLabel(l0);
            mv.visitLdcInsn(Type.getType("Lorg/sonatype/rest/api/PostServiceHandler;"));
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "isAssignableFrom", "(Ljava/lang/Class;)Z");
            Label l5 = new Label();
            mv.visitJumpInsn(IFEQ, l5);
            mv.visitLdcInsn(Type.getType("Lorg/sonatype/rest/api/PostServiceHandler;"));
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "cast", "(Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "org/sonatype/rest/api/PostServiceHandler");
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/sonatype/rest/api/PostServiceHandler", "formParams", "()Ljava/util/List;");
            mv.visitVarInsn(ASTORE, 9);
            mv.visitVarInsn(ALOAD, 9);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "size", "()I");
            mv.visitInsn(ICONST_1);
            mv.visitInsn(IADD);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
            mv.visitVarInsn(ASTORE, 6);
            mv.visitVarInsn(ALOAD, 9);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "size", "()I");
            mv.visitInsn(ICONST_1);
            mv.visitInsn(IADD);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            mv.visitVarInsn(ASTORE, 7);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitInsn(ICONST_0);
            mv.visitLdcInsn(Type.getType("Ljava/lang/String;"));
            mv.visitInsn(AASTORE);
            mv.visitVarInsn(ALOAD, 7);
            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitInsn(AASTORE);
            mv.visitInsn(ICONST_1);
            mv.visitVarInsn(ISTORE, 10);
            mv.visitVarInsn(ALOAD, 9);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;");
            mv.visitVarInsn(ASTORE, 11);
            Label l6 = new Label();
            mv.visitLabel(l6);
            mv.visitFrame(Opcodes.F_FULL, 12, new Object[]{className, "java/lang/String", "java/lang/String", "java/lang/String", "org/sonatype/rest/api/ServiceHandler", "java/lang/String", "[Ljava/lang/Class;", "[Ljava/lang/Object;", Opcodes.TOP, "java/util/List", Opcodes.INTEGER, "java/util/Iterator"}, 0, new Object[]{});
            mv.visitVarInsn(ALOAD, 11);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z");
            mv.visitJumpInsn(IFEQ, l5);
            mv.visitVarInsn(ALOAD, 11);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "java/lang/String");
            mv.visitVarInsn(ASTORE, 12);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitVarInsn(ILOAD, 10);
            mv.visitLdcInsn(Type.getType("Ljava/lang/String;"));
            mv.visitInsn(AASTORE);
            mv.visitVarInsn(ALOAD, 7);
            mv.visitVarInsn(ILOAD, 10);
            mv.visitIincInsn(10, 1);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
            mv.visitVarInsn(ALOAD, 12);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Field", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "java/lang/String");
            mv.visitInsn(AASTORE);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, "logger", "Lorg/slf4j/Logger;");
            mv.visitLdcInsn("Getting generated value {}");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
            mv.visitVarInsn(ALOAD, 12);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Field", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "info", "(Ljava/lang/String;Ljava/lang/Object;)V");
            mv.visitJumpInsn(GOTO, l6);
            mv.visitLabel(l5);
            mv.visitFrame(Opcodes.F_FULL, 8, new Object[]{className, "java/lang/String", "java/lang/String", "java/lang/String", "org/sonatype/rest/api/ServiceHandler", "java/lang/String", "[Ljava/lang/Class;", "[Ljava/lang/Object;"}, 0, new Object[]{});
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, "serviceEntity", "Lorg/sonatype/rest/api/ServiceEntity;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
            mv.visitVarInsn(ALOAD, 5);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
            mv.visitVarInsn(ASTORE, 9);
            mv.visitVarInsn(ALOAD, 9);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, "serviceEntity", "Lorg/sonatype/rest/api/ServiceEntity;");
            mv.visitVarInsn(ALOAD, 7);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitVarInsn(ASTORE, 8);
            mv.visitVarInsn(ALOAD, 8);
            mv.visitLabel(l1);
            mv.visitInsn(ARETURN);
            mv.visitLabel(l2);
            mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/Throwable"});
            mv.visitVarInsn(ASTORE, 8);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, "logger", "Lorg/slf4j/Logger;");
            mv.visitLdcInsn("createResponse");
            mv.visitVarInsn(ALOAD, 8);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "error", "(Ljava/lang/String;Ljava/lang/Throwable;)V");
            mv.visitVarInsn(ALOAD, 8);
            mv.visitMethodInsn(INVOKESTATIC, "com/google/sitebricks/headless/Reply", "with", "(Ljava/lang/Object;)Lcom/google/sitebricks/headless/Reply;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "com/google/sitebricks/headless/Reply", "error", "()Lcom/google/sitebricks/headless/Reply;");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(4, 13);
            mv.visitEnd();
        }

        if (postServiceHandlers.size() > 0) {
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

            moduleConfig.bind(clazz);
            moduleConfig.install(new com.google.sitebricks.SitebricksModule() {
                @Override
                protected void configureSitebricks() {
                    at(serviceDefinition.path() + "/:method/:id").serve(clazz);
                }
            });
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

    public static class StringServiceHandlerMediaType implements ServiceHandlerMediaType<String> {

        private String value;

        @Override
        public ServiceHandlerMediaType visit(java.lang.String value) {
            this.value = value;
            return this;
        }

        public java.lang.String getValue() {
            return value;
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




