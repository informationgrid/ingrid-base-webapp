package de.ingrid.admin;

import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.service.PlugDescriptionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationBean {
    private static final Log log = LogFactory.getLog(ConfigurationBean.class);

    @Autowired
    public ConfigurationBean(Config config, @Autowired(required = false) IConfig externalConfig, PlugDescriptionService plugDescriptionService) {
        PlugdescriptionCommandObject plugdescriptionFromProperties = config.getPlugdescriptionFromConfiguration();
        if (externalConfig != null) {
            externalConfig.addPlugdescriptionValues(plugdescriptionFromProperties);
        }

        // if a configuration was never written for the plugdescription then do not write one!
        // the proxyServiceUrl must be different from the default value, so we can check here
        // for valid properties
        String proxyServiceURL = plugdescriptionFromProperties.getProxyServiceURL();
        if (!"/ingrid-group:base-webapp".equals(proxyServiceURL) && !proxyServiceURL.isEmpty()) {
            try {
                plugDescriptionService.savePlugDescription(plugdescriptionFromProperties);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            log.warn("Plug Description not written, because the client name has not been changed! ('/ingrid-group:base-webapp')");
        }
    }
}
