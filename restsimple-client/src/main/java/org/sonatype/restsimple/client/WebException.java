/*
 * Copyright (c) 2011 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
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