package org.sonatype.client.json;

public class RuntimeJsonMappingException extends RuntimeException {
    public RuntimeJsonMappingException() {
    }

    public RuntimeJsonMappingException(Throwable cause) {
        super(cause);
    }

    public RuntimeJsonMappingException(String message) {
        super(message);
    }

    public RuntimeJsonMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
