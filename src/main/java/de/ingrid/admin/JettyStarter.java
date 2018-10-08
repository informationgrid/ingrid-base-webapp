/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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
    	//instance.config.getWebappDir();
    	//instance.start();
    }
    
    public static JettyStarter getInstance() {
        return instance;
    }
    
    public JettyStarter() throws Exception {
        configure();
        start();
    }
    
    public JettyStarter(boolean startImmediately) throws Exception {
        instance = this;
        configure();
        if (startImmediately) {
            start();
        }
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
        if (externalConfig != null) {
            externalConfig.initialize();
        } else {
            log.info("No external configuration found.");
        }
        
        // add external configurations to the plugdescription
        PlugdescriptionCommandObject plugdescriptionFromProperties = config.getPlugdescriptionFromConfiguration();
        if (externalConfig != null) {
            externalConfig.addPlugdescriptionValues( plugdescriptionFromProperties );
        }
        // if a configuration was never written for the plugdescription then do not write one!
        // the proxyServiceUrl must be different from the default value, so we can check here 
        // for valid propterties
        String proxyServiceURL = plugdescriptionFromProperties.getProxyServiceURL();
        if (!"/ingrid-group:base-webapp".equals( proxyServiceURL ) && !proxyServiceURL.isEmpty()) {
            (new PlugDescriptionService()).savePlugDescription( plugdescriptionFromProperties );
        } else {
            log.warn( "Plug Description not written, because the client name has not been changed! ('/ingrid-group:base-webapp')" );
        }
        
        Server server = new Server(port);
        server.setHandler(webAppContext);
        webAppContext.getSessionHandler().getSessionManager().setSessionCookie( "JSESSIONID_" + port );
        server.start();
    }

    public void setExternalConfig(IConfig config) {
        externalConfig = config;
    }
    public IConfig getExternalConfig() {
        return externalConfig;
        
    }
    
}
