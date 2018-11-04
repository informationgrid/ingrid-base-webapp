/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
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

import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.service.PlugDescriptionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Properties;

/**
 * This class starts a Jetty server where the webapp will be executed.
 * @author André Wallat
 *
 */
@Service
public class JettyStarter {
    private static final Log log = LogFactory.getLog(JettyStarter.class);

    public static Config baseConfig;

    public Config config;

    private IConfig externalConfig;


    @Autowired
    public JettyStarter(Config config, IConfig externalConfig, PlugDescriptionService plugDescriptionService) throws Exception {
        baseConfig = config;
        this.config = config;
        this.externalConfig = externalConfig;

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
            plugDescriptionService.savePlugDescription( plugdescriptionFromProperties );
        } else {
            log.warn( "Plug Description not written, because the client name has not been changed! ('/ingrid-group:base-webapp')" );
        }
    }

    // Use JettyStart(IConfig) instead to correctly configure iPlug
    @Deprecated
    public JettyStarter() throws Exception {
        start();
    }

    public JettyStarter(boolean startImmediately) throws Exception {
        if (startImmediately) {
            start();
        }
    }

    public JettyStarter( IConfig config ) throws Exception {
        this.setExternalConfig(config);
        start();
    }

    public JettyStarter(Class externalConfigClass) throws Exception {
        // manually create necessary beans for configuration we need during startup
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(Config.class);
        ctx.register(externalConfigClass);
        ctx.refresh();
        IConfig externalConfig = (IConfig) ctx.getBean(externalConfigClass);

        // set configurations to be used in start-method
        this.setExternalConfig(externalConfig);
        JettyStarter.baseConfig = ctx.getBean(Config.class);

        // this manual context is not used anymore so close it
        // during server start a new spring context is started
        ctx.close();

        start();
    }

    public static void main(String[] args) throws Exception {
    	new JettyStarter();
    }

    private void start() throws Exception {
        // we need to get the properties for server manually since spring beans are not initialized yet
        Properties config = this.getProperties();

        String webApp = (String) config.get("jetty.webapp");
        WebAppContext webAppContext = new WebAppContext(webApp, "/");

        int port = Integer.valueOf((String) config.get("jetty.port"));
        log.info("==================================================");
        log.info("Start server using directory \"" + webApp + "\" at port: " + port);
        log.info("==================================================");

        // do some initialization by reading properties and writing configuration files
        baseConfig.initialize();
        if (externalConfig != null) {
            externalConfig.initialize();
        } else {
            log.info("No external configuration found.");
        }

        Server server = new Server(port);
        server.setHandler(webAppContext);
        webAppContext.getSessionHandler().getSessionManager().setSessionCookie( "JSESSIONID_" + port );
        server.start();
    }

    private Properties getProperties() throws IOException {
        final Properties config = new Properties();
        config.load(JettyStarter.class.getResourceAsStream("/config.properties"));
        config.load(JettyStarter.class.getResourceAsStream("/config.override.properties"));
        return config;
    }

    private void setExternalConfig(IConfig config) {
        externalConfig = config;
    }

}
