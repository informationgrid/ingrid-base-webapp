/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.admin;

import de.ingrid.admin.service.CommunicationService;
import de.ingrid.admin.service.PlugDescriptionService;
import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.PlugDescription;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;


/**
 * The purpose of this class is to ensure that classes are configured correctly
 * during startup. Each class must implement IConfigurable so that its configure-
 * method can be called.
 */
@Service
public class InGridEnvironment {

    private static final Log log = LogFactory.getLog(InGridEnvironment.class);

    /**
     * All classes that implement IConfigurable will be ordered to execute their
     * configure-method. This way all classes are configured (correctly) during
     * startup.
     * AdminToolsController will not be used here, but its actions during creation
     * must be made before configurations can start.
     *
     * @param plugDescriptionService, which contains the PlugDescription
     * @param configurables,          classes that implement IConfigurable
     * @throws IOException on error
     */
    @Autowired
    public InGridEnvironment(final PlugDescriptionService plugDescriptionService,
                             final CommunicationService communicationService, // this is needed to be initialized
                             final IConfigurable... configurables) throws IOException {

        class ThreadedConfiguration extends Thread {
            private final PlugDescription plugDescription;

            public ThreadedConfiguration(PlugDescription plugDescription) {
                this.plugDescription = plugDescription;
            }

            public void run() {
                if (plugDescription != null) {
                    for (final IConfigurable configurable : configurables) {
                        try {
                            configurable.configure(plugDescription);
                        } catch (Exception ex) {
                            log.error("Exception during configuration: " + ex.getMessage());
                        }
                    }
                }
            }
        }

        final PlugDescription plugDescription = plugDescriptionService.getPlugDescription();

        // run in a thread to prevent blocking of webapp-start when connection to iBus cannot
        // be established
        ThreadedConfiguration threadedConfiguration = new ThreadedConfiguration(plugDescription);
        threadedConfiguration.start();

    }
}
