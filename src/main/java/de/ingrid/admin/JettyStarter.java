package de.ingrid.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * This class starts a Jetty server where the webapp will be executed.
 * @author André Wallat
 *
 */
public class JettyStarter {
    private static final Log log = LogFactory.getLog(JettyStarter.class);
    
    private static String DEFAULT_WEBAPP_DIR    = "webapp";
    
    private static int    DEFAULT_JETTY_PORT    = 8082;
    

    public static void main(String[] args) throws Exception {
        if (!System.getProperties().containsKey("jetty.webapp"))
            log.warn("Property 'jetty.webapp' not defined! Using default webapp directory, which is '"+DEFAULT_WEBAPP_DIR+"'.");
        if (!System.getProperties().containsKey("jetty.port"))
            log.warn("Property 'jetty.port' not defined! Using default port, which is '"+DEFAULT_JETTY_PORT+"'.");
        
        init();
    }
    
    private static void init() throws Exception {
        WebAppContext webAppContext = new WebAppContext(System.getProperty("jetty.webapp", DEFAULT_WEBAPP_DIR), "/");

        Server server = new Server(Integer.getInteger("jetty.port", DEFAULT_JETTY_PORT));
        server.setHandler(webAppContext);
        server.start();
    }

}
