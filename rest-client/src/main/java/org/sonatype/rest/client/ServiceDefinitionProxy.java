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
package org.sonatype.rest.client;

import com.ning.http.client.SimpleAsyncHttpClient;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.rest.api.ServiceDefinition;
import org.sonatype.rest.api.ServiceHandler;
import org.sonatype.rest.impl.JAXRSServiceDefinitionGenerator;

import java.lang.reflect.Constructor;

/**
 * Generates an implementation of {@link ServiceDefinitionClient} from a {@link ServiceDefinition}.
 */
public final class ServiceDefinitionProxy implements Opcodes {

    private final static Logger logger = LoggerFactory.getLogger(JAXRSServiceDefinitionGenerator.class);

    /**
     * Generates a {@link ServiceDefinitionClient} from a {@link ServiceDefinition} information.
     * @param serviceDefinition a {@link ServiceDefinition}
     * @return An implementation of {@ServiceDefinitionClient}
     */
    public final static ServiceDefinitionClient getProxy(ServiceDefinition serviceDefinition) {
        SimpleAsyncHttpClient sahc = new SimpleAsyncHttpClient.Builder().setUrl(serviceDefinition.path()).build();
        return generate(sahc, serviceDefinition);
    }

    private static ServiceDefinitionClient generate(SimpleAsyncHttpClient sahc, ServiceDefinition serviceDefinition) {

        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0;

        cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, "org/sonatype/rest/client/model/ServiceDefinitionClientImpl", null, "org/sonatype/rest/client/ServiceDefinitionClient", null);

        cw.visitInnerClass("java/util/Map$Entry", "java/util/Map", "Entry", ACC_PUBLIC + ACC_STATIC + ACC_ABSTRACT + ACC_INTERFACE);

        {
            fv = cw.visitField(ACC_PRIVATE + ACC_FINAL, "sahc", "Lcom/ning/http/client/SimpleAsyncHttpClient;", null, null);
            fv.visitEnd();
        }
        {
            fv = cw.visitField(ACC_PRIVATE + ACC_FINAL, "remoteServerUri", "Ljava/lang/String;", null, null);
            fv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Lcom/ning/http/client/SimpleAsyncHttpClient;Ljava/lang/String;)V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "org/sonatype/rest/client/ServiceDefinitionClient", "<init>", "()V");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitFieldInsn(PUTFIELD, "org/sonatype/rest/client/model/ServiceDefinitionClientImpl", "sahc", "Lcom/ning/http/client/SimpleAsyncHttpClient;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitFieldInsn(PUTFIELD, "org/sonatype/rest/client/model/ServiceDefinitionClientImpl", "remoteServerUri", "Ljava/lang/String;");
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 3);
            mv.visitEnd();
        }


        for (ServiceHandler serviceHandler : serviceDefinition.serviceHandlers()) {

            if (serviceHandler.getHttpMethod().name().equalsIgnoreCase("get")) {
                {
                    mv = cw.visitMethod(ACC_PUBLIC + ACC_VARARGS, "doGet", "([Ljava/lang/String;)Lcom/ning/http/client/Response;", null, null);
                    mv.visitCode();
                    Label l0 = new Label();
                    Label l1 = new Label();
                    Label l2 = new Label();
                    mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Throwable");
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitMethodInsn(INVOKESTATIC, "java/util/Arrays", "asList", "([Ljava/lang/Object;)Ljava/util/List;");
                    mv.visitVarInsn(ASTORE, 2);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitLdcInsn(serviceHandler.getMethod());
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitMethodInsn(INVOKESPECIAL, "org/sonatype/rest/client/model/ServiceDefinitionClientImpl", "createUri", "(Ljava/lang/String;Ljava/util/List;)Ljava/lang/String;");
                    mv.visitVarInsn(ASTORE, 3);
                    mv.visitLabel(l0);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, "org/sonatype/rest/client/model/ServiceDefinitionClientImpl", "sahc", "Lcom/ning/http/client/SimpleAsyncHttpClient;");
                    mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
                    mv.visitInsn(DUP);
                    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V");
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, "org/sonatype/rest/client/model/ServiceDefinitionClientImpl", "remoteServerUri", "Ljava/lang/String;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "com/ning/http/client/SimpleAsyncHttpClient", "get", "(Ljava/lang/String;)Ljava/util/concurrent/Future;");
                    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/concurrent/Future", "get", "()Ljava/lang/Object;");
                    mv.visitTypeInsn(CHECKCAST, "com/ning/http/client/Response");
                    mv.visitLabel(l1);
                    mv.visitInsn(ARETURN);
                    mv.visitLabel(l2);
                    mv.visitFrame(Opcodes.F_FULL, 4, new Object[]{"org/sonatype/rest/client/model/ServiceDefinitionClientImpl", "[Ljava/lang/String;", "java/util/List", "java/lang/String"}, 1, new Object[]{"java/lang/Throwable"});
                    mv.visitVarInsn(ASTORE, 4);
                    mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
                    mv.visitInsn(DUP);
                    mv.visitVarInsn(ALOAD, 4);
                    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/Throwable;)V");
                    mv.visitInsn(ATHROW);
                    mv.visitMaxs(3, 5);
                    mv.visitEnd();
                }
            }

            if (serviceHandler.getHttpMethod().name().equalsIgnoreCase("head")) {
                {
                    mv = cw.visitMethod(ACC_PUBLIC + ACC_VARARGS, "doHead", "([Ljava/lang/String;)Lcom/ning/http/client/Response;", null, null);
                    mv.visitCode();
                    Label l0 = new Label();
                    Label l1 = new Label();
                    Label l2 = new Label();
                    mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Throwable");
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitMethodInsn(INVOKESTATIC, "java/util/Arrays", "asList", "([Ljava/lang/Object;)Ljava/util/List;");
                    mv.visitVarInsn(ASTORE, 2);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitLdcInsn(serviceHandler.getMethod());
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitMethodInsn(INVOKESPECIAL, "org/sonatype/rest/client/model/ServiceDefinitionClientImpl", "createUri", "(Ljava/lang/String;Ljava/util/List;)Ljava/lang/String;");
                    mv.visitVarInsn(ASTORE, 3);
                    mv.visitLabel(l0);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, "org/sonatype/rest/client/model/ServiceDefinitionClientImpl", "sahc", "Lcom/ning/http/client/SimpleAsyncHttpClient;");
                    mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
                    mv.visitInsn(DUP);
                    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V");
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, "org/sonatype/rest/client/model/ServiceDefinitionClientImpl", "remoteServerUri", "Ljava/lang/String;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "com/ning/http/client/SimpleAsyncHttpClient", "head", "(Ljava/lang/String;)Ljava/util/concurrent/Future;");
                    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/concurrent/Future", "get", "()Ljava/lang/Object;");
                    mv.visitTypeInsn(CHECKCAST, "com/ning/http/client/Response");
                    mv.visitLabel(l1);
                    mv.visitInsn(ARETURN);
                    mv.visitLabel(l2);
                    mv.visitFrame(Opcodes.F_FULL, 4, new Object[]{"org/sonatype/rest/client/model/ServiceDefinitionClientImpl", "[Ljava/lang/String;", "java/util/List", "java/lang/String"}, 1, new Object[]{"java/lang/Throwable"});
                    mv.visitVarInsn(ASTORE, 4);
                    mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
                    mv.visitInsn(DUP);
                    mv.visitVarInsn(ALOAD, 4);
                    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/Throwable;)V");
                    mv.visitInsn(ATHROW);
                    mv.visitMaxs(3, 5);
                    mv.visitEnd();
                }
            }

            if (serviceHandler.getHttpMethod().name().equalsIgnoreCase("put")) {
                {
                    mv = cw.visitMethod(ACC_PUBLIC + ACC_VARARGS, "doPut", "([Ljava/lang/String;)Lcom/ning/http/client/Response;", null, null);
                    mv.visitCode();
                    Label l0 = new Label();
                    Label l1 = new Label();
                    Label l2 = new Label();
                    mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Throwable");
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitMethodInsn(INVOKESTATIC, "java/util/Arrays", "asList", "([Ljava/lang/Object;)Ljava/util/List;");
                    mv.visitVarInsn(ASTORE, 2);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitLdcInsn(serviceHandler.getMethod());
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitMethodInsn(INVOKESPECIAL, "org/sonatype/rest/client/model/ServiceDefinitionClientImpl", "createUri", "(Ljava/lang/String;Ljava/util/List;)Ljava/lang/String;");
                    mv.visitVarInsn(ASTORE, 3);
                    mv.visitLabel(l0);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, "org/sonatype/rest/client/model/ServiceDefinitionClientImpl", "sahc", "Lcom/ning/http/client/SimpleAsyncHttpClient;");
                    mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
                    mv.visitInsn(DUP);
                    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V");
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, "org/sonatype/rest/client/model/ServiceDefinitionClientImpl", "remoteServerUri", "Ljava/lang/String;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
                    mv.visitInsn(ACONST_NULL);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "com/ning/http/client/SimpleAsyncHttpClient", "put", "(Ljava/lang/String;Lcom/ning/http/client/BodyGenerator;)Ljava/util/concurrent/Future;");
                    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/concurrent/Future", "get", "()Ljava/lang/Object;");
                    mv.visitTypeInsn(CHECKCAST, "com/ning/http/client/Response");
                    mv.visitLabel(l1);
                    mv.visitInsn(ARETURN);
                    mv.visitLabel(l2);
                    mv.visitFrame(Opcodes.F_FULL, 4, new Object[]{"org/sonatype/rest/client/model/ServiceDefinitionClientImpl", "[Ljava/lang/String;", "java/util/List", "java/lang/String"}, 1, new Object[]{"java/lang/Throwable"});
                    mv.visitVarInsn(ASTORE, 4);
                    mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
                    mv.visitInsn(DUP);
                    mv.visitVarInsn(ALOAD, 4);
                    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/Throwable;)V");
                    mv.visitInsn(ATHROW);
                    mv.visitMaxs(3, 5);
                    mv.visitEnd();
                }
            }

            if (serviceHandler.getHttpMethod().name().equalsIgnoreCase("post")) {
                {
                    mv = cw.visitMethod(ACC_PUBLIC + ACC_VARARGS, "doPost", "(Ljava/util/Map;[Ljava/lang/String;)Lcom/ning/http/client/Response;", "(Ljava/util/Map<Ljava/lang/String;Ljava/lang/String;>;[Ljava/lang/String;)Lcom/ning/http/client/Response;", null);
                    mv.visitCode();
                    Label l0 = new Label();
                    Label l1 = new Label();
                    Label l2 = new Label();
                    mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Throwable");
                    mv.visitTypeInsn(NEW, "com/ning/http/client/FluentStringsMap");
                    mv.visitInsn(DUP);
                    mv.visitMethodInsn(INVOKESPECIAL, "com/ning/http/client/FluentStringsMap", "<init>", "()V");
                    mv.visitVarInsn(ASTORE, 3);
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "entrySet", "()Ljava/util/Set;");
                    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "iterator", "()Ljava/util/Iterator;");
                    mv.visitVarInsn(ASTORE, 4);
                    Label l3 = new Label();
                    mv.visitLabel(l3);
                    mv.visitFrame(Opcodes.F_APPEND, 2, new Object[]{"com/ning/http/client/FluentStringsMap", "java/util/Iterator"}, 0, null);
                    mv.visitVarInsn(ALOAD, 4);
                    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z");
                    Label l4 = new Label();
                    mv.visitJumpInsn(IFEQ, l4);
                    mv.visitVarInsn(ALOAD, 4);
                    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
                    mv.visitTypeInsn(CHECKCAST, "java/util/Map$Entry");
                    mv.visitVarInsn(ASTORE, 5);
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitVarInsn(ALOAD, 5);
                    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map$Entry", "getKey", "()Ljava/lang/Object;");
                    mv.visitTypeInsn(CHECKCAST, "java/lang/String");
                    mv.visitInsn(ICONST_1);
                    mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
                    mv.visitInsn(DUP);
                    mv.visitInsn(ICONST_0);
                    mv.visitVarInsn(ALOAD, 5);
                    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map$Entry", "getValue", "()Ljava/lang/Object;");
                    mv.visitTypeInsn(CHECKCAST, "java/lang/String");
                    mv.visitInsn(AASTORE);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "com/ning/http/client/FluentStringsMap", "add", "(Ljava/lang/String;[Ljava/lang/String;)Lcom/ning/http/client/FluentStringsMap;");
                    mv.visitInsn(POP);
                    mv.visitJumpInsn(GOTO, l3);
                    mv.visitLabel(l4);
                    mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitMethodInsn(INVOKESTATIC, "java/util/Arrays", "asList", "([Ljava/lang/Object;)Ljava/util/List;");
                    mv.visitVarInsn(ASTORE, 4);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitLdcInsn(serviceHandler.getMethod());
                    mv.visitVarInsn(ALOAD, 4);
                    mv.visitMethodInsn(INVOKESPECIAL, "org/sonatype/rest/client/model/ServiceDefinitionClientImpl", "createUri", "(Ljava/lang/String;Ljava/util/List;)Ljava/lang/String;");
                    mv.visitVarInsn(ASTORE, 5);
                    mv.visitTypeInsn(NEW, "com/ning/http/client/RequestBuilder");
                    mv.visitInsn(DUP);
                    mv.visitMethodInsn(INVOKESPECIAL, "com/ning/http/client/RequestBuilder", "<init>", "()V");
                    mv.visitLdcInsn("POST");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "com/ning/http/client/RequestBuilder", "setMethod", "(Ljava/lang/String;)Lcom/ning/http/client/RequestBuilder;");
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "com/ning/http/client/RequestBuilder", "setParameters", "(Lcom/ning/http/client/FluentStringsMap;)Lcom/ning/http/client/RequestBuilder;");
                    mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
                    mv.visitInsn(DUP);
                    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V");
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, "org/sonatype/rest/client/model/ServiceDefinitionClientImpl", "remoteServerUri", "Ljava/lang/String;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                    mv.visitVarInsn(ALOAD, 5);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "com/ning/http/client/RequestBuilder", "setUrl", "(Ljava/lang/String;)Lcom/ning/http/client/RequestBuilder;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "com/ning/http/client/RequestBuilder", "build", "()Lcom/ning/http/client/Request;");
                    mv.visitVarInsn(ASTORE, 6);
                    mv.visitLabel(l0);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, "org/sonatype/rest/client/model/ServiceDefinitionClientImpl", "sahc", "Lcom/ning/http/client/SimpleAsyncHttpClient;");
                    mv.visitVarInsn(ALOAD, 6);
                    mv.visitInsn(ACONST_NULL);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "com/ning/http/client/SimpleAsyncHttpClient", "post", "(Lcom/ning/http/client/Request;Lcom/ning/http/client/BodyGenerator;)Ljava/util/concurrent/Future;");
                    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/concurrent/Future", "get", "()Ljava/lang/Object;");
                    mv.visitTypeInsn(CHECKCAST, "com/ning/http/client/Response");
                    mv.visitLabel(l1);
                    mv.visitInsn(ARETURN);
                    mv.visitLabel(l2);
                    mv.visitFrame(Opcodes.F_FULL, 7, new Object[]{"org/sonatype/rest/client/model/ServiceDefinitionClientImpl", "java/util/Map", "[Ljava/lang/String;", "com/ning/http/client/FluentStringsMap", "java/util/List", "java/lang/String", "com/ning/http/client/Request"}, 1, new Object[]{"java/lang/Throwable"});
                    mv.visitVarInsn(ASTORE, 7);
                    mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
                    mv.visitInsn(DUP);
                    mv.visitVarInsn(ALOAD, 7);
                    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/Throwable;)V");
                    mv.visitInsn(ATHROW);
                    mv.visitMaxs(6, 8);
                    mv.visitEnd();
                }
            }

            if (serviceHandler.getHttpMethod().name().equalsIgnoreCase("delete")) {
                {
                    mv = cw.visitMethod(ACC_PUBLIC + ACC_VARARGS, "doDelete", "([Ljava/lang/String;)Lcom/ning/http/client/Response;", null, null);
                    mv.visitCode();
                    Label l0 = new Label();
                    Label l1 = new Label();
                    Label l2 = new Label();
                    mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Throwable");
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitMethodInsn(INVOKESTATIC, "java/util/Arrays", "asList", "([Ljava/lang/Object;)Ljava/util/List;");
                    mv.visitVarInsn(ASTORE, 2);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitLdcInsn(serviceHandler.getMethod());
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitMethodInsn(INVOKESPECIAL, "org/sonatype/rest/client/model/ServiceDefinitionClientImpl", "createUri", "(Ljava/lang/String;Ljava/util/List;)Ljava/lang/String;");
                    mv.visitVarInsn(ASTORE, 3);
                    mv.visitLabel(l0);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, "org/sonatype/rest/client/model/ServiceDefinitionClientImpl", "sahc", "Lcom/ning/http/client/SimpleAsyncHttpClient;");
                    mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
                    mv.visitInsn(DUP);
                    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V");
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, "org/sonatype/rest/client/model/ServiceDefinitionClientImpl", "remoteServerUri", "Ljava/lang/String;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "com/ning/http/client/SimpleAsyncHttpClient", "delete", "(Ljava/lang/String;)Ljava/util/concurrent/Future;");
                    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/concurrent/Future", "get", "()Ljava/lang/Object;");
                    mv.visitTypeInsn(CHECKCAST, "com/ning/http/client/Response");
                    mv.visitLabel(l1);
                    mv.visitInsn(ARETURN);
                    mv.visitLabel(l2);
                    mv.visitFrame(Opcodes.F_FULL, 4, new Object[]{"org/sonatype/rest/client/model/ServiceDefinitionClientImpl", "[Ljava/lang/String;", "java/util/List", "java/lang/String"}, 1, new Object[]{"java/lang/Throwable"});
                    mv.visitVarInsn(ASTORE, 4);
                    mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
                    mv.visitInsn(DUP);
                    mv.visitVarInsn(ALOAD, 4);
                    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/Throwable;)V");
                    mv.visitInsn(ATHROW);
                    mv.visitMaxs(3, 5);
                    mv.visitEnd();
                }
            }
        }
        {
            mv = cw.visitMethod(ACC_PRIVATE, "createUri", "(Ljava/lang/String;Ljava/util/List;)Ljava/lang/String;", "(Ljava/lang/String;Ljava/util/List<Ljava/lang/String;>;)Ljava/lang/String;", null);
            mv.visitCode();
            mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
            mv.visitInsn(DUP);
            mv.visitLdcInsn("/");
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
            mv.visitVarInsn(ASTORE, 3);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
            mv.visitLdcInsn("/");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
            mv.visitInsn(POP);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;");
            mv.visitVarInsn(ASTORE, 4);
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitFrame(Opcodes.F_APPEND, 2, new Object[]{"java/lang/StringBuilder", "java/util/Iterator"}, 0, null);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z");
            Label l1 = new Label();
            mv.visitJumpInsn(IFEQ, l1);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "java/lang/String");
            mv.visitVarInsn(ASTORE, 5);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
            mv.visitLdcInsn("/");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
            mv.visitInsn(POP);
            mv.visitJumpInsn(GOTO, l0);
            mv.visitLabel(l1);
            mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
            mv.visitVarInsn(ASTORE, 4);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "length", "()I");
            mv.visitInsn(ICONST_1);
            mv.visitInsn(ISUB);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "substring", "(II)Ljava/lang/String;");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(4, 6);
            mv.visitEnd();
        }
        cw.visitEnd();

        byte[] bytes = cw.toByteArray();

        try {
            ClassLoader cl = new ByteClassloader(bytes, ServiceDefinitionProxy.class.getClassLoader());
            Class<?> clazz = cl.loadClass("org.sonatype.rest.client.model.ServiceDefinitionClientImpl");

            Constructor constructor = clazz.getConstructor(new Class[]{SimpleAsyncHttpClient.class, String.class});

            return (ServiceDefinitionClient) constructor.newInstance(new Object[]{sahc, serviceDefinition.path()});
        } catch (Throwable e) {
            logger.error("generate", e);
        }
        return null;
    }

    private final static class ByteClassloader extends ClassLoader {

        private final byte[] clazzBytes;

        protected ByteClassloader(byte[] clazzBytes, ClassLoader parent) {
            super(parent);
            this.clazzBytes = clazzBytes;
        }

        protected Class findClass(String name) throws ClassNotFoundException {

            if (name.endsWith("ServiceDefinitionClientImpl")) {
                return defineClass(name, clazzBytes, 0, clazzBytes.length);
            } else {
                return super.findClass(name);
            }
        }
    }

}
