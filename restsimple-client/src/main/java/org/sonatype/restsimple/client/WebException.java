package org.sonatype.restsimple.client;

/**
 * An exception thrown when an unexpected exception
 */
public class WebException extends RuntimeException {

    private final int statusCode;
    private final String reasonPhrase;

    public WebException(int statusCode, String reasonPhrase) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
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