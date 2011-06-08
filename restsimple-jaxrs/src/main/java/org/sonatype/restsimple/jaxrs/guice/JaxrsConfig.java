/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.sonatype.restsimple.jaxrs.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.sonatype.restsimple.api.DefaultServiceDefinition;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.jaxrs.impl.ContentNegotiationFilter;
import org.sonatype.restsimple.jaxrs.impl.JAXRSServiceDefinitionGenerator;
import org.sonatype.restsimple.spi.NegotiationTokenGenerator;
import org.sonatype.restsimple.spi.RFC2295NegotiationTokenGenerator;
import org.sonatype.restsimple.spi.ServiceDefinitionConfig;
import org.sonatype.restsimple.spi.ServiceDefinitionGenerator;
import org.sonatype.restsimple.spi.ServiceHandlerMapper;
import org.sonatype.restsimple.spi.scan.Classes;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.inject.matcher.Matchers.annotatedWith;

/**
 * Base class for deploying {@link ServiceDefinition} to a JAXRS implementation.
 */
public class JaxrsConfig extends ServletModule implements ServiceDefinitionConfig {

    private Injector parent;
    private Injector injector;
    private final Map<String,String> jaxrsProperties;
    private final ServiceHandlerMapper mapper;
    private final List<Package> packages = new ArrayList<Package>();

    public JaxrsConfig() {
        this(null, new HashMap<String,String>());
    }

    public JaxrsConfig(ServiceHandlerMapper mapper) {
        this(null, new HashMap<String,String>(), mapper);
    }

    public JaxrsConfig(Map<String,String> jaxrsProperties) {
        this(null, jaxrsProperties);
    }

    public JaxrsConfig(Map<String,String> jaxrsProperties, ServiceHandlerMapper mapper) {
        this(null, jaxrsProperties, mapper);
    }

    public JaxrsConfig(Injector parent) {
        this(parent, new HashMap<String,String>());
    }

    public JaxrsConfig(Injector parent, ServiceHandlerMapper mapper) {
        this(parent, new HashMap<String,String>(), mapper);
    }

    public JaxrsConfig(Injector parent, Map<String,String> jaxrsProperties) {
        this(parent, jaxrsProperties, new ServiceHandlerMapper());
    }

    public JaxrsConfig(Injector parent, Map<String,String> jaxrsProperties, ServiceHandlerMapper mapper) {
        this.jaxrsProperties = jaxrsProperties;
        this.parent = parent;
        this.mapper = mapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void configureServlets() {
        injector = Guice.createInjector(new JaxrsModule(binder().withSource("[generated]"), mapper));
        List<ServiceDefinition> list = defineServices(parent != null ? parent : injector);
        ServiceDefinitionGenerator generator = injector.getInstance(JAXRSServiceDefinitionGenerator.class);

        if (list != null && list.size() > 0) {
            for (ServiceDefinition sd : list) {
                generator.generate(sd);
            }
        }

        Set<Class<?>> set = new HashSet<Class<?>>();
        for (Package pkg : packages) {
            //look for any classes annotated with @Path, @PathParam
            set.addAll(Classes.matching(
                    annotatedWith(Path.class).or(
                            annotatedWith(PathParam.class))
            ).in(pkg));
        }

        for (Class<?> clazz: set) {
            generator.generate(new DefaultServiceDefinition().extendWith(clazz));
        }

        jaxrsProperties.put("com.sun.jersey.api.json.POJOMappingFeature", "true");
        //jaxrsProperties.put("com.sun.jersey.config.feature.Trace", "true");

        filter("/*").through(ContentNegotiationFilter.class);
        serve("/*").with(GuiceContainer.class, jaxrsProperties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceDefinition> defineServices(Injector injector) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NegotiationTokenGenerator configureNegotiationTokenGenerator() {
        return new RFC2295NegotiationTokenGenerator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Injector injector() {
        return injector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void scan(Package packageName) {
        packages.add(packageName);
    }
}
