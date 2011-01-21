package org.sonatype.rest.spi;

import org.sonatype.rest.api.ServiceHandler;

import java.util.HashMap;

/**
 * A simple class generated resource can you to map the request to its associated {@link ServiceHandler} representation.
 */
public class ServiceHandlerMapper {

    private final HashMap<String, ServiceHandler> maps = new HashMap<String, ServiceHandler>();

    public ServiceHandlerMapper() {
    }

    /**
     * Ass a {@link ServiceHandler}
     * @param serviceHandler {@link ServiceHandler}
     * @return this
     */
    public ServiceHandlerMapper addServiceHandler(ServiceHandler serviceHandler) {
        maps.put(serviceHandler.getMethod(), serviceHandler);
        return this;
    }

    /**
     * Remove a {@link ServiceHandler}
     * @param serviceHandler {@link ServiceHandler}
     * @return this
     */
    public ServiceHandlerMapper removeServiceHandler(ServiceHandler serviceHandler) {
        maps.remove(serviceHandler.getMethod());
        return this;
    }

    /**
     * Map the current resource method to its's associated {@link ServiceHandler}
     * @param method The current request's method
     * @return a {@link ServiceHandler}, or null if not mapped.
     */
    public ServiceHandler map(String method) {
        return maps.get(method);
    }

}
