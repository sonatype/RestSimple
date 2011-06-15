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

import org.codehaus.jackson.map.ObjectMapper;

import java.util.Collections;
import java.util.Map;

/**
 * A {@link WebProxy} configuration object that allow configuring a generated {@link WebClient}
 */
public class WebProxyConfig {

    private final Map<String,String> bindings;
    private final Map<String,String> properties;
    private final ObjectMapper objectMapper;

    private WebProxyConfig(Map<String, String> bindings, Map<String, String> properties, ObjectMapper objectMapper){

        this.bindings = bindings;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * Returns an unmmodifiable Map of bindings
     * @return an unmmodifiable Map of bindings
     */
    public Map<String, String> getBindings() {
        return bindings;
    }

    /**
     * Returns an unmmodifiable Map of properties
     * @return an unmmodifiable Map of properties
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Return the Jackson's {@link ObjectMapper}
     * @return the Jackson's {@link ObjectMapper}
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static class Builder{

        private Map<String,String> bindings = Collections.<String, String>emptyMap();
        private Map<String,String> properties = Collections.<String, String>emptyMap();
        private ObjectMapper objectMapper = null;

        public Map<String, String> getBindings() {
            return bindings;
        }

        /**
         * Set a Map of bindings. Bindings will be used to replace value defined as :foo or {foo}
         * @param bindings a Map of bindings
         * @return  this
         */
        public Builder setBindings(Map<String, String> bindings) {
            this.bindings = bindings;
            return this;
        }

        /**
         * Set properties on the generated {@link WebClient}
         * @param properties properties on the generated {@link WebClient}
         * @return this
         */
        public Builder setProperties(Map<String, String> properties) {
            this.properties = properties;
            return this;
        }

        /**
         * The Jackson's {@link ObjectMapper}
         * @param objectMapper  The Jackson's {@link ObjectMapper}
         * @return this.
         */
        public Builder setObjectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        public WebProxyConfig build() {
            return new WebProxyConfig(Collections.unmodifiableMap(bindings),Collections.unmodifiableMap(properties),objectMapper);
        }

    }

}
