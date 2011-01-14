package org.sonatype.client;

import org.codehaus.jackson.annotate.JsonValue;

import java.util.Collections;
import java.util.Set;

public interface Applications extends Iterable<Application> {
    Applications EMPTY = new ImmutableApplications("EMPTY", Collections.<Application>emptySet());

    String getETag();

    @JsonValue
    Set<Application> getApplications();
}