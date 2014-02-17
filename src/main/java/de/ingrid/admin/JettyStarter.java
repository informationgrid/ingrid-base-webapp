package de.ingrid.admin;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.tngtech.configbuilder.ConfigBuilder;

/**
 * This class starts a Jetty server where the webapp will be executed.
 * @author André Wallat
 *
 */
public class JettyStarter {
    private static final Log log = LogFactory.getLog(JettyStarter.class);
    
	public static Config config;

    public static void main(String[] args) throws Exception {
    	// load configurations
    	config = new ConfigBuilder<Config>(Config.class).withCommandLineArgs(args).build();
    	
        init();
    }
    
    private static void init() throws Exception {
     // plug description
        String plugDescription = System.getProperty(IKeys.PLUG_DESCRIPTION);
        if (plugDescription == null) {
            plugDescription = "conf/plugdescription.xml";
            System.setProperty(IKeys.PLUG_DESCRIPTION, plugDescription);
            log.warn("plug description is not defined. using default: " + plugDescription);
        }

        // communication
        String communication = System.getProperty(IKeys.COMMUNICATION);
        if (communication == null) {
            communication = "conf/communication.xml";
            System.setProperty(IKeys.COMMUNICATION, communication);
            log.warn("commmunication is not defined. using default.");
        }
        if (!new File(communication).exists()) {
            log.warn("communication (" + communication + ") does not exists. please create one via ui setup.");
        }

        // indexing
        final String indexing = System.getProperty(IKeys.INDEXING);
        if (null == indexing) {
            System.setProperty(IKeys.INDEXING, "true");
        } else if (indexing.equals("false")) {
            System.clearProperty(IKeys.INDEXING);
        }
        
        
        log.info( "configure spring now" );
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.scan("de.ingrid");
        ctx.refresh();
        log.info( "configure spring now ... done" );
        
        WebAppContext webAppContext = new WebAppContext( config.getWebappDir(), "/");

        int port = config.getWebappPort();
        log.info("Start server using directory \"" + config.getWebappDir() + "\" at port: " + port);
        Server server = new Server(port);
        server.setHandler(webAppContext);
        server.start();
    }
    
}
