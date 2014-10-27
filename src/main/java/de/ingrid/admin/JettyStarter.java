package de.ingrid.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

import com.tngtech.configbuilder.ConfigBuilder;

import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.service.PlugDescriptionService;

/**
 * This class starts a Jetty server where the webapp will be executed.
 * @author André Wallat
 *
 */
public class JettyStarter {
    private static final Log log = LogFactory.getLog(JettyStarter.class);
    
	public Config config;
	
	private IConfig externalConfig = null;

	private static JettyStarter instance;

    public static void main(String[] args) throws Exception {
    	instance = new JettyStarter();
    	instance.config.getWebappDir();
    	//instance.start();
    }
    
    public static JettyStarter getInstance() {
        return instance;
    }
    
    public JettyStarter() throws Exception {
        configure();
        start();
    }
    
    public JettyStarter( IConfig config ) throws Exception {
        this.externalConfig = config;
        configure();
        start();
    }
    
    private void configure() {
        instance = this;
        try {
            // load configurations
            config = new ConfigBuilder<Config>(Config.class).build();
            
            //config = new ConfigBuilder<Config>(Config.class).withCommandLineArgs(args).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void start() throws Exception {
        WebAppContext webAppContext = new WebAppContext( config.getWebappDir(), "/");

        int port = config.getWebappPort();
        log.info("==================================================");
        log.info("Start server using directory \"" + config.getWebappDir() + "\" at port: " + port);
        log.info("==================================================");
        
        // do some initialization by reading properties and writing configuration files
        config.initialize();
        if (externalConfig != null)
            externalConfig.initialize();
        
        // add external configurations to the plugdescription
        PlugdescriptionCommandObject plugdescriptionFromProperties = config.getPlugdescriptionFromProperties();
        if (externalConfig != null) {
            externalConfig.addPlugdescriptionValues( plugdescriptionFromProperties );
        }
        // if a configuration was never written for the plugdescription then do not write one!
        // the proxyServiceUrl must be different from the default value, so we can check here 
        // for valid propterties 
        if (!"/ingrid-group:base-webapp".equals( plugdescriptionFromProperties.getProxyServiceURL() )) {
            (new PlugDescriptionService()).savePlugDescription( plugdescriptionFromProperties );
        } else {
            log.warn( "Plug Description not written, because the client name has not been changed! ('/ingrid-group:base-webapp')" );
        }
        
        Server server = new Server(port);
        server.setHandler(webAppContext);
        server.start();
    }

    public void setExternalConfig(IConfig config) {
        externalConfig = config;
    }
    public IConfig getExternalConfig() {
        return externalConfig;
        
    }
    
}
