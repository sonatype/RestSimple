package org.sonatype.rest.impl;

import com.google.inject.Binder;
import com.google.inject.Inject;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.sonatype.rest.api.ResourceBinder;
import org.sonatype.rest.api.ServiceDefinition;
import org.sonatype.rest.api.ServiceDefinitionGenerator;
import org.sonatype.rest.api.ServiceHandler;

/**
 * Generate a JAXRS resource, and bind it.
 */
public class JAXRSServiceDefinitionGenerator implements ServiceDefinitionGenerator, Opcodes {

    private final ResourceBinder binder;

    @Inject
    public JAXRSServiceDefinitionGenerator(ResourceBinder binder){
        this.binder = binder;
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
            av0.visit("value", serviceDefinition.basePath());
            av0.visitEnd();
        }
        {
            av0 = cw.visitAnnotation("Ljavax/ws/rs/Produces;", true);
            {
                AnnotationVisitor av1 = av0.visitArray("value");
                for (ServiceDefinition.Media m : serviceDefinition.mediaToProduce()) {
                    av1.visit(null, "application/" + m.name());
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
                    av1.visit(null, "application/" + m.name());
                }
                av1.visitEnd();
            }
            av0.visitEnd();
        }

        cw.visitInnerClass("javax/ws/rs/core/Response$ResponseBuilder", "javax/ws/rs/core/Response", "ResponseBuilder", ACC_PUBLIC + ACC_STATIC + ACC_ABSTRACT);

        {
            fv = cw.visitField(0, "delegate", "Lorg/sonatype/rest/api/ServiceEntity;", null, null);
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

                mv = cw.visitMethod(ACC_PUBLIC, method.toLowerCase(), "(Lorg/sonatype/rest/api/ServiceHandler;Ljava/lang/String;)Ljavax/ws/rs/core/Response;", null, null);
                {
                    av0 = mv.visitAnnotation("Ljavax/ws/rs/" + method.toUpperCase() + ";", true);
                    av0.visitEnd();
                }
                {
                    av0 = mv.visitAnnotation("Ljavax/ws/rs/Path;", true);
                    av0.visit("value", "{id}");
                    av0.visitEnd();
                }
                {
                    av0 = mv.visitParameterAnnotation(0, "Ljavax/ws/rs/PathParam;", true);
                    av0.visit("value", "deletePerson(id)");
                    av0.visitEnd();
                }
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitVarInsn(ALOAD, 2);
                mv.visitMethodInsn(INVOKESPECIAL, "org/sonatype/rest/model/ServiceDescriptionResource", "createResponse", "(Lorg/sonatype/rest/api/ServiceHandler;Ljava/lang/String;)Ljavax/ws/rs/core/Response;");
                mv.visitVarInsn(ASTORE, 3);
                mv.visitVarInsn(ALOAD, 3);
                mv.visitInsn(ARETURN);
                mv.visitMaxs(3, 4);
                mv.visitEnd();
            }
        }
        {
            mv = cw.visitMethod(ACC_PRIVATE, "createResponse", "(Lorg/sonatype/rest/api/ServiceHandler;Ljava/lang/String;)Ljavax/ws/rs/core/Response;", null, null);
            mv.visitCode();
            Label l0 = new Label();
            Label l1 = new Label();
            Label l2 = new Label();
            mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Throwable");
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/sonatype/rest/api/ServiceHandler", "getMethod", "()Ljava/lang/String;");
            mv.visitVarInsn(ASTORE, 3);
            mv.visitLabel(l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, "org/sonatype/rest/model/ServiceDescriptionResource", "delegate", "Lorg/sonatype/rest/api/ServiceEntity;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
            mv.visitVarInsn(ALOAD, 3);
            mv.visitInsn(ICONST_1);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
            mv.visitInsn(DUP);
            mv.visitInsn(ICONST_0);
            mv.visitLdcInsn(Type.getType("Ljava/lang/String;"));
            mv.visitInsn(AASTORE);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
            mv.visitVarInsn(ASTORE, 4);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitInsn(ICONST_1);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            mv.visitInsn(DUP);
            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitInsn(AASTORE);
            mv.visitInsn(ICONST_0);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitMethodInsn(INVOKESTATIC, "javax/ws/rs/core/Response", "ok", "(Ljava/lang/Object;)Ljavax/ws/rs/core/Response$ResponseBuilder;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "javax/ws/rs/core/Response$ResponseBuilder", "build", "()Ljavax/ws/rs/core/Response;");
            mv.visitLabel(l1);
            mv.visitInsn(ARETURN);
            mv.visitLabel(l2);
            mv.visitFrame(Opcodes.F_FULL, 4, new Object[]{"org/sonatype/rest/model/ServiceDescriptionResource", "org/sonatype/rest/api/ServiceHandler", "java/lang/String", "java/lang/String"}, 1, new Object[]{"java/lang/Throwable"});
            mv.visitVarInsn(ASTORE, 4);
            mv.visitMethodInsn(INVOKESTATIC, "javax/ws/rs/core/Response", "serverError", "()Ljavax/ws/rs/core/Response$ResponseBuilder;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "javax/ws/rs/core/Response$ResponseBuilder", "build", "()Ljavax/ws/rs/core/Response;");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(6, 5);
            mv.visitEnd();
        }
        cw.visitEnd();

        byte[] bytes = cw.toByteArray();

        try {
            ClassLoader cl = new ByteClassloader(bytes, this.getClass().getClassLoader());
            Class<?> clazz = cl.loadClass("org.sonatype.rest.model.ServiceDescriptionResource");
            
            binder.bind(clazz);

        } catch (Throwable e) {
            // TODO: LOGME
            e.printStackTrace();
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




