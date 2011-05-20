package org.sonatype.restsimple.client;

import java.io.IOException;

/**
 * An exception thrown when an unexpected exception
 */
public class WebException extends RuntimeException {

    private final int statusCode;
    private final String reasonPhrase;

    public WebException(int statusCode, String reasonPhrase) {
        super(new IOException(String.format("Server returned status %s with reason phrase %s", statusCode, reasonPhrase)));
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
    }

    public WebException(Throwable cause) {
        super(cause);
        this.statusCode = 500;
        this.reasonPhrase = "Server error";
    }

    /**
     * Return the response status code.
     * @return the response status code.
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Return the response's reason phrase.
     * @return the response's reason phrase.
     */
    public String getReasonPhrase() {
        return reasonPhrase;
    }

    @Override
    public String toString() {
        return "WebException{" +
                "statusCode=" + statusCode +
                ", reasonPhrase='" + reasonPhrase + '\'' +
                '}';
    }
}