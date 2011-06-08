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
 * Represent a Media type consumed or produced by an {@link Action}.
 */
public final class MediaType {
    public static MediaType JSON = new MediaType("application","json");
    public static MediaType XML = new MediaType("application","xml");
    public static MediaType TEXT = new MediaType("text","plain");
    public static MediaType HTML = new MediaType("text","html");
    
    private final String type;
    private final String subType;

    /**
     * Create a media type that support wildcard
     */
    public MediaType() {
        this("*", "*");
    }

    /**
     * Create a media type with the wildcard as sub type, e.g "application/*"
     * @param type
     */
    public MediaType(String type) {
        this(type, "*");
    }

    /**
     * Create a media type representation
     * @param type the type, or first part of "application/json"
     * @param subType  the type, or the last part of "application/json"
     */
    public MediaType(String type, String subType) {
        this.type = type;
        this.subType = subType;
    }

    /**
     * Return the Media's Type, e.g the first part of "application/json".
     * @return the Media's Type, e.g the first part of "application/json".
     */
    public String type(){
        return type;
    }

    /**
     * Return the Media's subtype, e.g the last part of "application/json".
     * @return he Media's subtype, e.g the last part of "application/json".
     */
    public String subType(){
        return subType;
    }

    /**
     * Return the complete Media Type.
     * @return
     */
    public String toMediaType(){
        return type + "/" + subType();
    }

    @Override
    public String toString() {
        return toMediaType();
    }

}