package org.sonatype.server.resources;

import org.sonatype.client.Application;
import org.sonatype.client.Applications;
import org.sonatype.etag.ETagChangeListener;
import org.sonatype.server.store.ApplicationStore;

import java.util.concurrent.atomic.AtomicReference;

public class JsonApplicationStore implements ApplicationStore {
    private final ApplicationStore delegate;
    private final AtomicReference<JsonApplications> jsonApplication = new AtomicReference<JsonApplications>(new JsonApplications(Applications.EMPTY));

    public JsonApplicationStore(ApplicationStore delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getEnvironment() {
        return delegate.getEnvironment();
    }

    @Override
    public String getETag() {
        return delegate.getETag();
    }

    @Override
    public Application getApplication(String application) {
        return delegate.getApplication(application);
    }

    @Override
    public JsonApplications getApplications() {
        Applications applications = delegate.getApplications();
        JsonApplications jsonApplications = jsonApplication.get();

        if (!applications.getETag().equals(jsonApplications.getETag())) {
            jsonApplications = new JsonApplications(applications);
            jsonApplication.set(jsonApplications);
        }

        return jsonApplications;
    }

    @Override
    public void addApplication(Application application) {
        delegate.addApplication(application);
    }

    @Override
    public void removeApplication(Application application) {
        delegate.removeApplication(application);
    }

    @Override
    public void removeApplication(String application) {
        delegate.removeApplication(application);
    }

    @Override
    public void addETagChangeListener(ETagChangeListener l) {
        delegate.addETagChangeListener(l);
    }

    @Override
    public void removeETagChangeListener(ETagChangeListener l) {
        delegate.removeETagChangeListener(l);
    }

}
