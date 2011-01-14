package org.sonatype.server.resources;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

public class CachingJacksonJsonProvider extends JacksonJsonProvider {
    private static final Charset utf8 = Charset.forName("UTF-8");

    public CachingJacksonJsonProvider() {
    }

    public CachingJacksonJsonProvider(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public void writeTo(Object value, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {

        if (value instanceof CachingJsonObject) {
            String encoding = (String) httpHeaders.getFirst("Content-Encoding");
            byte[] b;
            if (encoding != null && encoding.equals("gzip")) {
                b = ((CachingJsonObject) value).getCompressedJsonBytes();
            } else {
                b = ((CachingJsonObject) value).getJsonBytes();
            }
            entityStream.write(b);
            entityStream.write(new byte[0]);
        } else {
            super.writeTo(value, type, genericType, annotations, mediaType, httpHeaders, entityStream);
        }
    }
}

