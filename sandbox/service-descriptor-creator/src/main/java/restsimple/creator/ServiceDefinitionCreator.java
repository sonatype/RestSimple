package org.sonatype.restsimple.creator;

import org.sonatype.restsimple.api.ServiceDefinition;

//
// Ultimately this might not come from a class at all
//
public interface ServiceDefinitionCreator
{
    ServiceDefinition create( Class<?> application );
}
