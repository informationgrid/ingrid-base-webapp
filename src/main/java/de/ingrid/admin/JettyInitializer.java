/*-
 * **************************************************-
 * InGrid Base-Webapp
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.admin;

import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;

import java.util.ArrayList;
import java.util.List;

public class JettyInitializer implements JettyServerCustomizer {

    private final String[] jettyBaseResources;

    public JettyInitializer(String[] jettyBaseResources) {
        this.jettyBaseResources = jettyBaseResources;
    }

    @Override
    public void customize(Server server) {
        WebAppContext handler = (WebAppContext) server.getHandler();
        handler.setWelcomeFiles(new String[]{"index.jsp"});

        ResourceFactory resourceFactory = ResourceFactory.of(handler);

        List<Resource> resources = new ArrayList<>();
        for (String jettyBaseResource : jettyBaseResources) {
            Resource res = resourceFactory.newResource(jettyBaseResource);
            resources.add(res);
        }

        // fix correct redirect when behind proxy (see: https://github.com/jetty/jetty.project/issues/11947)
        HttpConnectionFactory httpConfig = server.getConnectors()[0].getConnectionFactory(HttpConnectionFactory.class);
        httpConfig.getHttpConfiguration().setRelativeRedirectAllowed(false);

        handler.setBaseResource(ResourceFactory.combine(resources));
    }
}
