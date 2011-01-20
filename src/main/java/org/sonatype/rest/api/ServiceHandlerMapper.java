package org.sonatype.rest.api;

import java.util.HashMap;

public class ServiceHandlerMapper {

    private final HashMap<String, ServiceHandler> maps = new HashMap<String, ServiceHandler>();

    public ServiceHandlerMapper() {
    }

    public ServiceHandlerMapper addServiceHandler(ServiceHandler serviceHandler) {
        maps.put(serviceHandler.getMethod(), serviceHandler);
        return this;
    }

    public ServiceHandlerMapper removeServiceHandler(ServiceHandler serviceHandler) {
        maps.remove(serviceHandler.getMethod());
        return this;
    }

    public ServiceHandler map(String method) {
        return maps.get(method);
    }

}
