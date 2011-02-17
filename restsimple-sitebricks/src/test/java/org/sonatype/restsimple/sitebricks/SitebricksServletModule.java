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
package org.sonatype.restsimple.sitebricks;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.ServletModule;
import org.sonatype.restsimple.api.DeleteServiceHandler;
import org.sonatype.restsimple.api.GetServiceHandler;
import org.sonatype.restsimple.api.MediaType;
import org.sonatype.restsimple.api.PostServiceHandler;
import org.sonatype.restsimple.api.PutServiceHandler;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.api.ServiceEntity;
import org.sonatype.restsimple.sitebricks.guice.SitebricksModule;
import org.sonatype.restsimple.tests.AddressBookMediaType;
import org.sonatype.restsimple.tests.AddressBookServiceEntity;

public class SitebricksServletModule extends ServletModule {

    @Override
    protected void configureServlets() {
        Injector injector = Guice.createInjector(new SitebricksModule(binder()));
        PostServiceHandler postServiceHandler = new PostServiceHandler("id", "updateAddressBook");
        postServiceHandler.addFormParam("update");
        postServiceHandler.addFormParam("update2");

        ServiceEntity serviceEntity = new AddressBookServiceEntity();
        ServiceDefinition serviceDefinition = injector.getInstance(ServiceDefinition.class);
        serviceDefinition
                .producing(new MediaType(AddressBookServiceEntity.APPLICATION, AddressBookServiceEntity.JSON))
                .producing(new MediaType(AddressBookServiceEntity.APPLICATION, AddressBookServiceEntity.XML))
                .consuming(MediaType.JSON)
                .consuming(MediaType.XML)
                .withHandler(new PutServiceHandler("id", "createAddressBook"))
                .withHandler(new GetServiceHandler("id", "getAddressBook", AddressBookMediaType.class))
                .withHandler(postServiceHandler)
                .withHandler(new DeleteServiceHandler("id", "deleteAddressBook"))
                .usingEntity(serviceEntity)
                .bind();

        serviceDefinition = injector.getInstance(ServiceDefinition.class);
        serviceDefinition
                .withPath("/foo")
                .consuming(MediaType.JSON)
                .consuming(MediaType.XML)
                .withHandler(new PutServiceHandler("id", "createAddressBook").producing(new MediaType(AddressBookServiceEntity.APPLICATION, AddressBookServiceEntity.JSON)))
                .withHandler(new GetServiceHandler("id", "getAddressBook", AddressBookMediaType.class).producing(new MediaType(AddressBookServiceEntity.APPLICATION, AddressBookServiceEntity.JSON)))
                .withHandler(postServiceHandler.producing(new MediaType(AddressBookServiceEntity.APPLICATION, AddressBookServiceEntity.JSON)))
                .withHandler(new DeleteServiceHandler("id", "deleteAddressBook").producing(new MediaType(AddressBookServiceEntity.APPLICATION, AddressBookServiceEntity.JSON)))
                .usingEntity(serviceEntity)
                .bind();
    }

}

