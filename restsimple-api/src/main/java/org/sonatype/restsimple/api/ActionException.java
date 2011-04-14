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
package org.sonatype.restsimple.api;

/**
 * A runtime exception thrown from an {@link Action}. The content of this exception will be send back to a remote client.
 */
public class ActionException extends RuntimeException {

    private final int statusCode;
    private final String reasonPhrase;

    public ActionException(Throwable t) {
        super(t);
        this.statusCode = 500;
        this.reasonPhrase = "";
    }

    /**
     * Create a RuntimeException with a status code. The reason phrase will be empty.
     * @param statusCode the status code
     */
    public ActionException(int statusCode) {
        super();
        this.statusCode = statusCode;
        this.reasonPhrase = "";
    }

    /**
     * Create a RuntimeException with a status code and a reason phrase
     * @param statusCode the status code
     * @param reasonPhrase the status text
     */
    public ActionException(int statusCode, String reasonPhrase) {
        super(reasonPhrase + " status code: " + statusCode);
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
    }

    /**
     * The reason phase to send back to the client
     * @return The reason phase to send back to the client
     */
    public String getReasonPhrase(){
        return reasonPhrase;
    }

    /**
     * Return the status code to send back to the client
     * @return the status code to send back to the clien
     */
    public int getStatusCode(){
        return statusCode;
    }

}
