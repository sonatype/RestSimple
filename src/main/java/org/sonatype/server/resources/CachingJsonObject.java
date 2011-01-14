package org.sonatype.server.resources;

public interface CachingJsonObject {
    String getJson();

    byte[] getJsonBytes();

    byte[] getCompressedJsonBytes();
}
