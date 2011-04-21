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
package org.sonatype.restsimple.tests.templating;

import org.sonatype.restsimple.api.Action;
import org.sonatype.restsimple.api.DefaultServiceDefinition;
import org.sonatype.restsimple.api.DeleteServiceHandler;
import org.sonatype.restsimple.api.GetServiceHandler;
import org.sonatype.restsimple.api.MediaType;
import org.sonatype.restsimple.api.PostServiceHandler;
import org.sonatype.restsimple.api.PutServiceHandler;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.common.test.addressbook.AddressBookAction;
import org.sonatype.restsimple.templating.HtmlTemplateGenerator;
import org.sonatype.sisu.template.VelocityTemplater;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

public class ServiceDefinitionTemplateTest {

    private final static String SD = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n" +
            "        \"http://www.w3.org/TR/html4/loose.dtd\">\n" +
            "<html>\n" +
            "<head>\n" +
            "    <title>ServiceDefinitionTemplate</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "<h1>ServiceDefinition</h1>\n" +
            "\n" +
            "<div>\n" +
            "    <p>\n" +
            "        ROOT URI: /serviceDefinition\n" +
            "    </p>\n" +
            "\n" +
            "    <p>\n" +
            "        Consuming:\n" +
            "    <ul>\n" +
            "                    <li>application/vnd.org.sonatype.rest+json</li>\n" +
            "            </ul>\n" +
            "    </p>\n" +
            "    <p>\n" +
            "        Producing:\n" +
            "    <ul>\n" +
            "                    <li>application/vnd.org.sonatype.rest+json</li>\n" +
            "                    <li>application/vnd.org.sonatype.rest+xml</li>\n" +
            "            </ul>\n" +
            "    </p>\n" +
            "\n" +
            "</div>\n" +
            "        <h2> ServiceHandler </h2>\n" +
            "\n" +
            "    <div>\n" +
            "        <p>\n" +
            "            URI: /serviceDefinition/createAddressBook/:ad\n" +
            "        </p>\n" +
            "\n" +
            "        <p>\n" +
            "            Method: PUT\n" +
            "        </p>\n" +
            "\n" +
            "                    <p>\n" +
            "            Consuming: application/vnd.org.sonatype.rest+json\n" +
            "            </p>\n" +
            "        \n" +
            "        <p>\n" +
            "            Producing:\n" +
            "        <ul>\n" +
            "                    </ul>\n" +
            "        </p>\n" +
            "        <p>\n" +
            "            Action: AddressBookAction\n" +
            "        </p>\n" +
            "    </div>\n" +
            "        <h2> ServiceHandler </h2>\n" +
            "\n" +
            "    <div>\n" +
            "        <p>\n" +
            "            URI: /serviceDefinition/getAddressBook/:ad\n" +
            "        </p>\n" +
            "\n" +
            "        <p>\n" +
            "            Method: GET\n" +
            "        </p>\n" +
            "\n" +
            "                    <p>\n" +
            "            Consuming: application/vnd.org.sonatype.rest+json\n" +
            "            </p>\n" +
            "        \n" +
            "        <p>\n" +
            "            Producing:\n" +
            "        <ul>\n" +
            "                    </ul>\n" +
            "        </p>\n" +
            "        <p>\n" +
            "            Action: AddressBookAction\n" +
            "        </p>\n" +
            "    </div>\n" +
            "        <h2> ServiceHandler </h2>\n" +
            "\n" +
            "    <div>\n" +
            "        <p>\n" +
            "            URI: /serviceDefinition/updateAddressBook/:ad\n" +
            "        </p>\n" +
            "\n" +
            "        <p>\n" +
            "            Method: POST\n" +
            "        </p>\n" +
            "\n" +
            "                    <p>\n" +
            "            Consuming: application/vnd.org.sonatype.rest+json\n" +
            "            </p>\n" +
            "        \n" +
            "        <p>\n" +
            "            Producing:\n" +
            "        <ul>\n" +
            "                    </ul>\n" +
            "        </p>\n" +
            "        <p>\n" +
            "            Action: AddressBookAction\n" +
            "        </p>\n" +
            "    </div>\n" +
            "        <h2> ServiceHandler </h2>\n" +
            "\n" +
            "    <div>\n" +
            "        <p>\n" +
            "            URI: /serviceDefinition/deleteAddressBook/:ad\n" +
            "        </p>\n" +
            "\n" +
            "        <p>\n" +
            "            Method: DELETE\n" +
            "        </p>\n" +
            "\n" +
            "                    <p>\n" +
            "            Consuming: application/vnd.org.sonatype.rest+json\n" +
            "            </p>\n" +
            "        \n" +
            "        <p>\n" +
            "            Producing:\n" +
            "        <ul>\n" +
            "                    </ul>\n" +
            "        </p>\n" +
            "        <p>\n" +
            "            Action: AddressBookAction\n" +
            "        </p>\n" +
            "    </div>\n" +
            "    </body>\n" +
            "</html>";

    @Test
    public void basicServiceDefinitionTest() throws Exception {

        Action action = new AddressBookAction();
        PostServiceHandler postServiceHandler = new PostServiceHandler("/updateAddressBook/:ad", action);
        postServiceHandler.addFormParam("update");
        postServiceHandler.addFormParam("update2");
        
        ServiceDefinition serviceDefinition = new DefaultServiceDefinition();
        serviceDefinition
                .withPath("/serviceDefinition")
                .producing(new MediaType(AddressBookAction.APPLICATION, AddressBookAction.JSON))
                .producing(new MediaType(AddressBookAction.APPLICATION, AddressBookAction.XML))
                .consuming(new MediaType(AddressBookAction.APPLICATION, AddressBookAction.JSON))
                .withHandler(new PutServiceHandler("/createAddressBook/:ad", action))
                .withHandler(new GetServiceHandler("/getAddressBook/:ad", action))
                .withHandler(postServiceHandler)
                .withHandler(new DeleteServiceHandler("/deleteAddressBook/:ad", action));


        HtmlTemplateGenerator generator = new HtmlTemplateGenerator(new VelocityTemplater());
        String sdString = generator.generateTemplate(serviceDefinition);
        assertEquals(sdString, SD);
    }
}
