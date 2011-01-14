package org.sonatype.client;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

// TODO: An Application offer one or many service. This is where we need to decide how service are defined.
public class Service {

    private final String service;

    @JsonCreator
    public Service(@JsonProperty("service") String service) {
        this.service = service;
    }

    public String getService() {
        return service;
    }
}
