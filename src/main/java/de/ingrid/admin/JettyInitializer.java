package de.ingrid.admin;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;

public class JettyInitializer implements JettyServerCustomizer {
    
    @Override
    public void customize(Server server) {
        ((WebAppContext) server.getHandler()).setBaseResource(new ResourceCollection(
                new String[]{"src/main/webapp", "target/base-webapp"}));
    }    
}
