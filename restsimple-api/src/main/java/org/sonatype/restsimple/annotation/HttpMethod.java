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
package org.sonatype.restsimple.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HttpMethod {
    
    /**
     * HTTP Get method
     */
    public static final String GET="Get";
    /**
     * HTTP Post method
     */
    public static final String POST="Post";
    /**
     * HTTP PUT method
     */
    public static final String PUT="PUT";
    /**
     * HTTP Delete method
     */
    public static final String DELETE="Delete";
    /**
     * HTTP Head method
     */
    public static final String HEAD="Head";
    /**
     * HTTP OPTIONS method
     */
    public static final String OPTIONS="OPTIONS";
    
    /**
     * Specifies the name of a HTTP method. E.g. "Get".
     */
    String value();        
}
