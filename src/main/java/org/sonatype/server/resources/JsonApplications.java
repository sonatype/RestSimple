package org.sonatype.server.resources;

import org.sonatype.client.Application;
import org.sonatype.client.Applications;
import org.sonatype.client.json.ApplicationJson;
import org.sonatype.client.json.JacksonProxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

public class JsonApplications implements Applications, CachingJsonObject {
    private static final ApplicationJson APPLICATION_JSON = JacksonProxy.newProxyInstance(ApplicationJson.class);

    private final Applications delegate;
    private final String json;
    private static final Charset utf8 = Charset.forName("UTF-8");
    private final byte[] jsonByte;
    private final byte[] compressedJsonByte;

    public JsonApplications(Applications delegate) {
        this.delegate = delegate;
        this.json = APPLICATION_JSON.writeApplications(delegate);
        jsonByte = json.getBytes(utf8);
        compressJson();
        compressedJsonByte = compressJson();
    }

    public JsonApplications(Applications delegate, String json) {
        this.delegate = delegate;
        this.json = json;
        jsonByte = json.getBytes(utf8);
        compressedJsonByte = compressJson();
    }

    @Override
    public String getETag() {
        return delegate.getETag();
    }

    @Override
    public Set<Application> getApplications() {
        return delegate.getApplications();
    }

    @Override
    public Iterator<Application> iterator() {
        return delegate.iterator();
    }

    @Override
    public String getJson() {
        return json;
    }

    public byte[] getJsonBytes() {
        return jsonByte;
    }

    public byte[] getCompressedJsonBytes() {
        return compressedJsonByte;
    }

    private byte[] compressJson() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024 * 50);
            GZIPOutputStream gos = new GZIPOutputStream(bos);
            gos.write(jsonByte);
            gos.close();
            return bos.toByteArray();
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
