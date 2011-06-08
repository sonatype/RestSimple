/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.restsimple.client;

import java.io.IOException;

/**
 * An exception thrown when an unexpected exception occurs with the {@link WebProxy} and {@link WebClient}
 */
public class WebException extends RuntimeException {

    private final int statusCode;
    private final String reasonPhrase;

    public WebException(int statusCode, String reasonPhrase) {
        super(new IOException(String.format("Server returned status %s with reason phrase %s", statusCode, reasonPhrase)));
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
    }

    /**
     * Create an exception with the Throwable cause.
     * @param cause a {@link Throwable}
     */
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