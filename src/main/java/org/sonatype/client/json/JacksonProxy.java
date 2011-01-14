package org.sonatype.client.json;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

public final class JacksonProxy {
    private JacksonProxy() {
    }

    public static <T> T newProxyInstance(Class<T> type, Class<?>... additionalTypes) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationConfig.Feature.WRITE_NULL_PROPERTIES, false);
        objectMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        return newProxyInstance(objectMapper, type, additionalTypes);
    }

    public static <T> T newProxyInstance(ObjectMapper objectMapper, Class<T> type, Class<?>... additionalTypes) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) classLoader = type.getClassLoader();

        Class<?>[] interfaces = new Class<?>[additionalTypes.length + 1];
        interfaces[0] = type;
        System.arraycopy(additionalTypes, 0, interfaces, 1, additionalTypes.length);

        InvocationHandler invocationHandler = new JsonInvocationHandler(objectMapper, interfaces);
        Object proxy = Proxy.newProxyInstance(classLoader, interfaces, invocationHandler);
        return type.cast(proxy);
    }

    public static ObjectMapper getObjectMapper(Object proxy) {
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(proxy);
        if (invocationHandler instanceof JsonInvocationHandler) {
            JsonInvocationHandler jsonInvocationHandler = (JsonInvocationHandler) invocationHandler;
            return jsonInvocationHandler.getObjectMapper();
        }
        throw new IllegalArgumentException("Proxy is not a JacksonProxy");
    }

    public static class JsonInvocationHandler implements InvocationHandler {
        private final ObjectMapper objectMapper;
        private final Map<Method, Handler> handlers = new LinkedHashMap<Method, Handler>();

        public JsonInvocationHandler(ObjectMapper objectMapper, Class<?>[] interfaces) {
            if (objectMapper == null) throw new NullPointerException("mapper is null");
            if (interfaces == null) throw new NullPointerException("type is null");

            this.objectMapper = objectMapper;

            for (Class<?> type : interfaces) {
                for (Method method : type.getMethods()) {
                    String name = method.getName();
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    Class<?> returnType = method.getReturnType();

                    if (name.startsWith("read")) {
                        if (parameterTypes.length == 1 && !returnType.equals(Void.TYPE)) {
                            ReadHandler handler = new ReadHandler(objectMapper, method.getGenericReturnType(), canThrowIOException(method));
                            handlers.put(method, handler);
                        }
                    } else if (name.startsWith("write")) {
                        if (parameterTypes.length == 2 && Void.TYPE.equals(returnType)) {
                            WriteHandler handler = new WriteHandler(objectMapper, null, canThrowIOException(method));
                            handlers.put(method, handler);
                        } else if (parameterTypes.length == 1 && !returnType.equals(Void.TYPE)) {
                            WriteHandler handler = new WriteHandler(objectMapper, returnType, canThrowIOException(method));
                            handlers.put(method, handler);
                        }

                    }
                }
            }
        }

        public ObjectMapper getObjectMapper() {
            return objectMapper;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Handler handler = handlers.get(method);
            if (handler == null) {
                throw new UnsupportedOperationException(method.toString());
            }
            return handler.invoke(args);
        }

        private static boolean canThrowIOException(Method method) {
            for (Class<?> exceptionType : method.getExceptionTypes()) {
                if (IOException.class.isAssignableFrom(exceptionType)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static interface Handler {
        Object invoke(Object[] args) throws Throwable;
    }

    private static class ReadHandler implements Handler {
        private final ObjectMapper mapper;
        private final JavaType javaType;
        private final boolean canThrowIoException;

        public ReadHandler(ObjectMapper mapper, Type type, boolean canThrowIoException) {
            this.mapper = mapper;
            javaType = TypeFactory.fromType(type);
            this.canThrowIoException = canThrowIoException;
        }

        @Override
        public Object invoke(Object[] args) throws Throwable {
            try {
                Object source = args[0];
                return read(source);
            } catch (IOException e) {
                if (canThrowIoException) {
                    throw e;
                } else {
                    throw new RuntimeJsonMappingException(e);
                }
            }
        }

        public Object read(Object source) throws IOException {
            if (source == null) {
                throw new NullPointerException("source is null");
            }

            JsonParser parser;
            if (source instanceof String) {
                String string = (String) source;
                parser = mapper.getJsonFactory().createJsonParser(string);
            } else if (source instanceof byte[]) {
                byte[] bytes = (byte[]) source;
                parser = mapper.getJsonFactory().createJsonParser(bytes);
            } else if (source instanceof Reader) {
                Reader reader = (Reader) source;
                parser = mapper.getJsonFactory().createJsonParser(reader);
                parser.disableFeature(JsonParser.Feature.AUTO_CLOSE_SOURCE);
            } else if (source instanceof InputStream) {
                InputStream inputStream = (InputStream) source;
                parser = mapper.getJsonFactory().createJsonParser(inputStream);
                parser.disableFeature(JsonParser.Feature.AUTO_CLOSE_SOURCE);
            } else if (source instanceof File) {
                File file = (File) source;
                parser = mapper.getJsonFactory().createJsonParser(file);
            } else if (source instanceof URL) {
                URL url = (URL) source;
                parser = mapper.getJsonFactory().createJsonParser(url);
            } else {
                throw new UnsupportedOperationException("Unsupported source type " + source.getClass() + " for JSON read method");
            }

            try {
                return mapper.readValue(parser, javaType);
            } finally {
                parser.close();
            }
        }
    }

    private static class WriteHandler implements Handler {
        private final ObjectMapper mapper;
        private final Class<?> returnType;
        private final boolean canThrowIoException;

        public WriteHandler(ObjectMapper mapper, Class<?> returnType, boolean canThrowIoException) {
            this.mapper = mapper;
            this.returnType = returnType;
            this.canThrowIoException = canThrowIoException;
        }

        @Override
        public Object invoke(Object[] args) throws Throwable {
            try {
                Object value = args[0];
                if (returnType == null) {
                    Object target = args[1];
                    write(value, target);
                    return null;
                } else if (String.class.equals(returnType)) {
                    StringWriter stringWriter = new StringWriter();
                    write(value, stringWriter);
                    return stringWriter.toString();
                } else if (byte[].class.equals(returnType)) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    write(value, out);
                    return out.toByteArray();
                } else {
                    throw new UnsupportedOperationException("Unsupported target type " + returnType + " for JSON write method");
                }
            } catch (IOException e) {
                if (canThrowIoException) {
                    throw e;
                } else {
                    throw new RuntimeJsonMappingException(e);
                }
            }
        }

        public void write(Object value, Object target) throws IOException {
            if (target == null) {
                throw new NullPointerException("target is null");
            }

            JsonGenerator generator;
            if (target instanceof Writer) {
                Writer writer = (Writer) target;
                generator = mapper.getJsonFactory().createJsonGenerator(writer);
                generator.disableFeature(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
            } else if (target instanceof OutputStream) {
                OutputStream outputStream = (OutputStream) target;
                generator = mapper.getJsonFactory().createJsonGenerator(outputStream, JsonEncoding.UTF8);
                generator.disableFeature(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
            } else if (target instanceof File) {
                File file = (File) target;
                generator = mapper.getJsonFactory().createJsonGenerator(file, JsonEncoding.UTF8);
            } else {
                throw new UnsupportedOperationException("Unsupported target type " + target.getClass() + " for JSON write method");
            }

            if (mapper.getSerializationConfig().isEnabled(SerializationConfig.Feature.INDENT_OUTPUT)) {
                generator.useDefaultPrettyPrinter();
            }

            try {
                mapper.writeValue(generator, value);
            } finally {
                generator.close();
            }
        }
    }
}
