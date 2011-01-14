package org.sonatype.client;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

public class Application {
    private final String host;
    private final String environment;
    private final Set<Service> metaData;

    public Application(String host, String environment, Service... services) {
        this(host, environment, Arrays.asList(services));
    }

    @JsonCreator
    public Application(
            @JsonProperty("host") String host,
            @JsonProperty("environment") String environment,
            @JsonProperty("meta-data") Collection<Service> services) {

        if (host == null) {
            throw new NullPointerException("host is null");
        }
        if (environment == null) {
            throw new NullPointerException("environment is null");
        }

        this.host = host;
        this.environment = environment;
        if (services == null) {
            this.metaData = emptySet();
        } else {
            this.metaData = unmodifiableSet(new HashSet<Service>(services));
        }
    }

    public String getName() {
        return host;
    }

    public String getEnvironment() {
        return environment;
    }

    public Set<Service> getMetaData() {
        return metaData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Application that = (Application) o;

        if (!host.equals(that.host)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return host.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Applications");
        sb.append("{host='").append(host).append('\'');
        sb.append(", environment='").append(environment).append('\'');
        sb.append(", meta-data=").append(metaData);
        sb.append('}');
        return sb.toString();
    }
}
