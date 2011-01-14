package org.sonatype.etag;

public interface ETag {

    void regenerateEtag();

    String getEtag();

    void addChangeListener(ETagChangeListener listener);

    void removeChangeListener(ETagChangeListener listener);
}




