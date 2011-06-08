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
package org.sonatype.restsimple.jaxrs.impl;

import com.sun.jersey.spi.resource.Singleton;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Singleton
public class GenericMessageBodyWriter implements MessageBodyWriter<Object> {
    public long getSize(final Object t, final Class<?> type, final Type genericType,
                        final Annotation[] annotations, final MediaType mediaType) {
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWriteable(final Class<?> type, final Type genericType,
                               final Annotation[] annotations, final MediaType mediaType) {
        return mediaType.isCompatible(MediaType.TEXT_PLAIN_TYPE) ||
                mediaType.isCompatible(new MediaType("application", "vnd.org.sonatype.rest+txt")) ||
                mediaType.isCompatible(MediaType.TEXT_HTML_TYPE) ||
                mediaType.isCompatible(new MediaType("application", "vnd.org.sonatype.rest+html"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeTo(final Object t, final Class<?> type, final Type genericType,
                        final Annotation[] annotations, final MediaType mediaType,
                        final MultivaluedMap<String, Object> httpHeaders,
                        final OutputStream entityStream) throws IOException, WebApplicationException {
        entityStream.write(t.toString().getBytes("UTF-8"));
    }
}

