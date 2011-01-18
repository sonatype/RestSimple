package org.sonatype.rest;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Generate a JAXRS resource, and bind it.
 */
public class JAXRSServiceDefinitionGenerator implements ServiceDefinitionGenerator, Opcodes {

    @Inject
    Injector injector;

    @Override
    public void generate(ServiceDefinition serviceDefinition) {

        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0;

        cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, "org/sonatype/server/resources/ServiceDescriptionResource", null, "java/lang/Object", null);

        {
            av0 = cw.visitAnnotation("Ljavax/ws/rs/Path;", true);
            av0.visit("value", serviceDefinition.basePath());
            av0.visitEnd();
        }
        {
            av0 = cw.visitAnnotation("Ljavax/ws/rs/Produces;", true);
            {
                AnnotationVisitor av1 = av0.visitArray("value");
                for (ServiceDefinition.Media m : serviceDefinition.mediaToProduce()) {
                    av1.visit(null, m);
                }
                av1.visitEnd();
            }
            av0.visitEnd();
        }
        {
            av0 = cw.visitAnnotation("Ljavax/ws/rs/Consumes;", true);
            {
                AnnotationVisitor av1 = av0.visitArray("value");
                for (ServiceDefinition.Media m : serviceDefinition.mediaToConsume()) {
                    av1.visit(null, m);
                }
                av1.visitEnd();
            }
            av0.visitEnd();
        }

        cw.visitInnerClass("javax/ws/rs/core/Response$ResponseBuilder", "javax/ws/rs/core/Response", "ResponseBuilder", ACC_PUBLIC + ACC_STATIC + ACC_ABSTRACT);

        {
            fv = cw.visitField(0, "delegate", "Lorg/sonatype/rest/ServiceEntity;", null, null);
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
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }

        for (ServiceHandler serviceHandler : serviceDefinition.serviceHandlers()) {
            {
                String method = serviceHandler.getHttpMethod().name();
                mv = cw.visitMethod(ACC_PUBLIC, method.toLowerCase(), "(Lorg/sonatype/rest/ServiceHandler;)Ljavax/ws/rs/core/Response;", null, null);
                {
                    av0 = mv.visitAnnotation("Ljavax/ws/rs/" + method.toUpperCase()  +";", true);
                    av0.visitEnd();
                }

                if (serviceHandler.getPath() != null) {
                    {
                        av0 = mv.visitParameterAnnotation(0, "Ljavax/ws/rs/PathParam;", true);
                        av0.visit("value", serviceHandler.getPath());
                        av0.visitEnd();
                    }
                }
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, "org/sonatype/server/resources/ServiceDescriptionResource", "delegate", "Lorg/sonatype/rest/ServiceEntity;");
                mv.visitMethodInsn(INVOKEVIRTUAL, "org/sonatype/rest/ServiceHandler", "delegate" + method.substring(0,1).toUpperCase() + method.substring(1), "(Lorg/sonatype/rest/ServiceEntity;)Lorg/sonatype/rest/ServiceHandler;");
                mv.visitMethodInsn(INVOKESPECIAL, "org/sonatype/server/resources/ServiceDescriptionResource", "createResponse", "(Lorg/sonatype/rest/ServiceHandler;)Ljavax/ws/rs/core/Response;");
                mv.visitVarInsn(ASTORE, 2);
                mv.visitVarInsn(ALOAD, 2);
                mv.visitInsn(ARETURN);
                mv.visitMaxs(3, 3);
                mv.visitEnd();
            }
        }
        {
            mv = cw.visitMethod(ACC_PRIVATE, "createResponse", "(Lorg/sonatype/rest/ServiceHandler;)Ljavax/ws/rs/core/Response;", null, null);
            mv.visitCode();
            mv.visitMethodInsn(INVOKESTATIC, "javax/ws/rs/core/Response", "ok", "()Ljavax/ws/rs/core/Response$ResponseBuilder;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "javax/ws/rs/core/Response$ResponseBuilder", "build", "()Ljavax/ws/rs/core/Response;");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 2);
            mv.visitEnd();
        }
        cw.visitEnd();

        byte[] bytes = cw.toByteArray();
    }
}

