package org.sonatype.rest.api;

/**
 * Transforms the result {@link org.sonatype.rest.api.ServiceEntity} method invocation into a format expected by the client.
 * As an ezample, a {@link org.sonatype.rest.api.ServiceEntity} may produce String, which can be transformed by that class
 * into JSON or AML representation.
 */
public interface ServiceHandlerMediaType {

    /**
     * Transform an Object into another representation
     * @param object an Object i
     * @return a transformed instance of {@link ServiceHandlerMediaType}
     */
    ServiceHandlerMediaType visit(Object object);

}
