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
