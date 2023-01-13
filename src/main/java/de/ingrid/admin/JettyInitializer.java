/*-
 * **************************************************-
 * InGrid Base-Webapp
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.admin;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;

public class JettyInitializer implements JettyServerCustomizer {

    private final String[] jettyBaseResources;

    public JettyInitializer(String[] jettyBaseResources) {
        this.jettyBaseResources = jettyBaseResources;
    }

    @Override
    public void customize(Server server) {
        WebAppContext handler = (WebAppContext) server.getHandler();
        handler.setWelcomeFiles(new String[]{"index.jsp"});
        handler.setBaseResource(new ResourceCollection(jettyBaseResources));
    }
}
