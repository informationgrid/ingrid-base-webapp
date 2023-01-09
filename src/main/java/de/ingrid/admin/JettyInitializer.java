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
