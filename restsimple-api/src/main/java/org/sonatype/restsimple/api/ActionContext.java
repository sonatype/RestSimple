package org.sonatype.restsimple.api;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

public class ActionContext {

    private final Map<String, Collection<String>> formParam;
    private final Map<String, Collection<String>> headers;
    private final InputStream inputStream;
    private final ServiceDefinition.METHOD methodName;
    private final String pathName;
    private final String pathValue;

    public ActionContext(ServiceDefinition.METHOD methodName, Map<String, Collection<String>> headers, Map<String, Collection<String>> formParam, InputStream inputStream, String pathName, String pathValue) {
        this.formParam = formParam;
        this.methodName = methodName;
        this.inputStream = inputStream;
        this.headers = headers;
        this.pathName = pathName;
        this.pathValue = pathValue;
    }

    public Map<String, Collection<String>> formParams() {
        return formParam;

    }

    public Map<String, Collection<String>> headers() {
        return headers;
    }

    public InputStream inputStream() {
        return inputStream;
    }

    public ServiceDefinition.METHOD method() {
        return methodName;
    }

    public String pathValue(){
        return pathValue;
    }

    public String pathName(){
        return pathName;
    }

}
