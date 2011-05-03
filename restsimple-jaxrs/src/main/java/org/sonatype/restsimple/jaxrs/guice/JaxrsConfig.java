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

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.jaxrs.impl.ContentNegotiationFilter;
import org.sonatype.restsimple.jaxrs.impl.JAXRSServiceDefinitionGenerator;
import org.sonatype.restsimple.spi.NegotiationTokenGenerator;
import org.sonatype.restsimple.spi.RFC2295NegotiationTokenGenerator;
import org.sonatype.restsimple.spi.ServiceDefinitionConfig;
import org.sonatype.restsimple.spi.ServiceDefinitionGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for deploying {@link ServiceDefinition} to a JAXRS implementation.
 */
public abstract class JaxrsConfig extends ServletModule implements ServiceDefinitionConfig {

    private Binder binder;
    private final Map<String,String> jaxrsProperties;

    public JaxrsConfig() {
        this(null, new HashMap<String,String>());
    }

    public JaxrsConfig(Map<String,String> jaxrsProperties) {
        this(null, jaxrsProperties);
    }

    public JaxrsConfig(Binder binder) {
        this(binder, new HashMap<String,String>());
    }

    public JaxrsConfig(Binder binder, Map<String,String> jaxrsProperties) {
        this.jaxrsProperties = jaxrsProperties;
        this.binder = binder;
    }

    @Override
    protected final void configureServlets() {
        Injector injector = null;
        if (binder != null) {
            Module module = new JaxrsModule(binder.withSource("[generated]"), configureNegotiationTokenGenerator());
            injector = Guice.createInjector(module);
        } else {
            injector = Guice.createInjector(new JaxrsModule(binder().withSource("[generated]"), configureNegotiationTokenGenerator()));
        }

        List<ServiceDefinition> list = defineServices(injector);
        if (list != null && list.size() > 0) {
            ServiceDefinitionGenerator generator = injector.getInstance(JAXRSServiceDefinitionGenerator.class);
            for (ServiceDefinition sd : list) {
                generator.generate(sd);
            }
        }

        jaxrsProperties.put("com.sun.jersey.api.json.POJOMappingFeature", "true");
        //initParams.put("com.sun.jersey.config.feature.Trace", "true");

        filter("/*").through(ContentNegotiationFilter.class);
        serve("/*").with(GuiceContainer.class, jaxrsProperties);
    }


    @Override
    public NegotiationTokenGenerator configureNegotiationTokenGenerator() {
        return new RFC2295NegotiationTokenGenerator();
    }

}
