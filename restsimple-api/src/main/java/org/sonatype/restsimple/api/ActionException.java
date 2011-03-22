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
 * An exception thrown from an {@link Action}
 */
public class ActionException extends Exception {

    private final int statusCode;
    private final String statusText;

    public ActionException(int statusCode) {
        super();
        this.statusCode = statusCode;
        this.statusText = "";
    }

    public ActionException(int statusCode, String statusText) {
        super(statusText + " status code: " + statusCode);
        this.statusCode = statusCode;
        this.statusText = statusText;
    }

    public String getStatusText(){
        return statusText;
    }

    public int getStatusCode(){
        return statusCode;
    }

}
