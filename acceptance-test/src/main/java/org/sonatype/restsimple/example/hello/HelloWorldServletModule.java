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
package org.sonatype.restsimple.example.hello;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.sonatype.restsimple.api.GetServiceHandler;
import org.sonatype.restsimple.api.MediaType;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.api.ServiceEntity;
import org.sonatype.restsimple.jaxrs.guice.JaxrsModule;

public class HelloWorldServletModule extends com.google.inject.servlet.ServletModule {

    @Override
    protected void configureServlets() {
        Injector injector = Guice.createInjector(new JaxrsModule(binder().withSource("[generated]")));

        ServiceEntity serviceEntity = new HelloWorldServiceEntity();
        ServiceDefinition serviceDefinition = injector.getInstance(ServiceDefinition.class);
        serviceDefinition
                .withHandler(new GetServiceHandler("name", "sayPlainTextHello", HelloWorldMediaType.class).producing(
                        new MediaType(HelloWorldServiceEntity.APPLICATION, HelloWorldServiceEntity.TXT)))
                .withHandler(new GetServiceHandler("name", "sayPlainXmlHello", HelloWorldMediaType.class).producing(
                        new MediaType(HelloWorldServiceEntity.APPLICATION, HelloWorldServiceEntity.XML)))
                .withHandler(new GetServiceHandler("name", "sayPlainHtmlHello", HelloWorldMediaType.class).producing(
                        new MediaType(HelloWorldServiceEntity.APPLICATION, HelloWorldServiceEntity.HTML)))
                .withHandler(new GetServiceHandler("name", "sayPlainJsonHello", HelloWorldMediaType.class).producing(
                        new MediaType(HelloWorldServiceEntity.APPLICATION, HelloWorldServiceEntity.JSON)))
                .usingEntity(serviceEntity)
                .bind();

        serve("/*").with(GuiceContainer.class);

    }

}

