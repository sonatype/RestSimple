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
package org.sonatype.restsimple.creator;

import org.sonatype.restsimple.api.MediaType;

import java.util.HashMap;

/**
 * A configuration object used by {@link org.sonatype.restsimple.creator.ServiceDefinitionCreator} when generating
 * {@link org.sonatype.restsimple.api.ServiceDefinition}
 *
 * The following table described how a class' methods are mapped to {@link org.sonatype.restsimple.api.ServiceHandler}
 * and {@link org.sonatype.restsimple.api.ServiceDefinition}
 * <p/>
 * Method starts with name       Mapped to             URI
 * =============================================================================
 * create                  PostServiceHandler     POST /create
 * read                    GetServiceHandler      GET /read/{anything}
 * reads                   GetServiceHandler      GET /reads
 * update                  PutServiceHandler      UPDATE /update
 * delete                  DELETEServiceHandler   DELETE /delete/{anything}
 */
public class ServiceDefinitionCreatorConfig {

    public static enum METHOD {
        GET, POST, PUT, DELETE
    }

    public final static String APPLICATION = "application";
    public final static String JSON = "json";

    public final static String CREATE = "create";
    public final static String CREATES = "creates";
    public final static String READS = "reads";
    public final static String READ = "read";
    public final static String UPDATE = "update";
    public final static String DELETE = "delete";

    private final static MediaType APPLICATION_JSON = new MediaType(APPLICATION, JSON);

    private final HashMap<String, MethodMapper> methodMappers = new HashMap<String, MethodMapper>();

    /**
     * Create a ServiceDefinitionCreatorConfig that use the default mapping.
     */
    public ServiceDefinitionCreatorConfig() {
        addMethodMapper(new MethodMapper(CREATE, METHOD.POST, APPLICATION_JSON, APPLICATION_JSON));
        addMethodMapper(new MethodMapper(READ, METHOD.GET, APPLICATION_JSON, APPLICATION_JSON));
        addMethodMapper(new MethodMapper(READS, METHOD.GET, APPLICATION_JSON, APPLICATION_JSON));
        addMethodMapper(new MethodMapper(UPDATE, METHOD.PUT, APPLICATION_JSON, APPLICATION_JSON));
        addMethodMapper(new MethodMapper(DELETE, METHOD.DELETE, APPLICATION_JSON, APPLICATION_JSON));
    }

    /**
     * Add a {@link MethodMapper} used by a {@link org.sonatype.restsimple.creator.ServiceDefinitionCreator} when
     * generating {@link org.sonatype.restsimple.api.ServiceDefinition} from any POJO/Class.
     * @param methodMapper
     * @return this
     */
    public ServiceDefinitionCreatorConfig addMethodMapper(MethodMapper methodMapper) {
        methodMappers.put(methodMapper.getMethodMappedTo(), methodMapper);
        return this;
    }

    /**
     * Return the {@link MethodMapper} associated with the method's name, or null if there is no mapping.
     * @param methodName a method name
     * @return
     */
    public MethodMapper map(String methodName) {

        for (String m : methodMappers.keySet()) {

            if (methodName.startsWith(m)) {
                return methodMappers.get(m);
            }
        }
        return null;
    }

    /**
     * A utility class used when translating a Class into a ServiceDefinition. This class hold the information
     * used to generate the ServiceDefinition and it's associated URI mapping.
     */
    public static final class MethodMapper {
        private final String methodMappedTo;
        private final METHOD method;
        private final MediaType produceMediaType;
        private final MediaType consumeMediaType;

        public MethodMapper(String mappedTo, METHOD method) {
            this.methodMappedTo = mappedTo;
            this.method = method;
            this.produceMediaType = APPLICATION_JSON;
            this.consumeMediaType = APPLICATION_JSON;
        }

        public MethodMapper(String mappedTo, METHOD method, MediaType produceMediaType, MediaType consumeMediaType) {
            this.methodMappedTo = mappedTo;
            this.method = method;
            this.produceMediaType = produceMediaType;
            this.consumeMediaType = consumeMediaType;
        }

        public String getMethodMappedTo() {
            return methodMappedTo;
        }

        public METHOD getMethod() {
            return method;
        }

        public MediaType getProduceMediaType() {
            return produceMediaType;
        }

        public MediaType getConsumeMediaType() {
            return consumeMediaType;
        }

        @Override
        public String toString() {
            return "MethodMapper{" +
                    "methodMappedTo='" + methodMappedTo + '\'' +
                    ", method=" + method +
                    ", produceMediaType=" + produceMediaType +
                    ", consumeMediaType=" + consumeMediaType +
                    '}';
        }
    }
}
