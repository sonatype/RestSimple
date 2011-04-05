package org.sonatype.restsimple.client;

public class WebException extends RuntimeException {

    private final int statusCode;
    private final String reasonPhrase;

    public WebException(int statusCode, String reasonPhrase) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

}