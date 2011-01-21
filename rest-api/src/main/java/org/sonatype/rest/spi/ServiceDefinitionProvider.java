package org.sonatype.rest.spi;

import com.google.inject.Provider;
import org.sonatype.rest.api.ServiceDefinition;

/**
 * A Guice Provider for {@link org.sonatype.rest.api.ServiceDefinition}
 */
public interface ServiceDefinitionProvider extends Provider<ServiceDefinition>{
}
