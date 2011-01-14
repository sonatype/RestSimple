package org.sonatype.client;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class ImmutableApplications implements Applications {
    private final String etag;
    private final Set<Application> applications;

    public ImmutableApplications(String etag, Collection<Application> applications) {
        if (etag == null) {
            throw new NullPointerException("etag is null");
        }
        if (applications == null) {
            throw new NullPointerException("applications is null");
        }
        this.etag = etag;
        this.applications = Collections.unmodifiableSet(new LinkedHashSet<Application>(applications));
    }

    @Override
    public String getETag() {
        return etag;
    }

    @Override
    public Set<Application> getApplications() {
        return applications;
    }

    @Override
    public Iterator<Application> iterator() {
        return applications.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImmutableApplications that = (ImmutableApplications) o;

        return etag.equals(that.etag);
    }

    @Override
    public int hashCode() {
        return etag.hashCode();
    }

    @Override                                                                                

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ImmutableApplications");
        sb.append("{etag='").append(etag).append('\'');
        sb.append(", applications=").append(applications);
        sb.append('}');
        return sb.toString();
    }
}
