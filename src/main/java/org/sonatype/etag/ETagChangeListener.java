package org.sonatype.etag;

public interface ETagChangeListener {

    void onChange(ETagChangeListenerEvent e);

}
