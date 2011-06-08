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
