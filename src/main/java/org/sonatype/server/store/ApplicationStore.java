package org.sonatype.server.store;

import org.sonatype.client.Application;
import org.sonatype.client.Applications;
import org.sonatype.etag.ETagChangeListener;


public interface ApplicationStore {
    
    String getEnvironment();

    String getETag();

    Application getApplication(String application);

    Applications getApplications();

    void addApplication(Application application);

    void removeApplication(Application application);

    void removeApplication(String application);

    void addETagChangeListener(ETagChangeListener l);

    void removeETagChangeListener(ETagChangeListener l);
}