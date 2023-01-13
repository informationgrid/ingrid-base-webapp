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
