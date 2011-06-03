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
package org.sonatype.restsimple.templating;

import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.sisu.template.Templater;
import org.sonatype.sisu.template.loader.FileTemplateLoader;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple Velocity template generator who generates HTML view of a ServiceDefinition.
 */
public class HtmlTemplateGenerator implements TemplateGenerator {

    private final Map<String, Object> context = new HashMap<String, Object>();

    private final Templater templater;

    public HtmlTemplateGenerator(Templater templater) {
        this.templater = templater;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateTemplate(ServiceDefinition serviceDefinition) throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        URL sdUrl = cl.getResource("templates/ServiceDefinitions.vm");
        File template = new File(sdUrl.toURI());

        context.put("sd", serviceDefinition);

        Writer writer = new StringWriter();
        templater.setTemplateLoader( new FileTemplateLoader());
        templater.renderTemplate( template.getAbsolutePath(), context, writer );
        return writer.toString();
    }

}
