/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

public class ContextListener implements ServletContextListener {

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ContextListener.class);

    public void contextInitialized(final ServletContextEvent contextEvent) {
        // communication
//        String communication = System.getProperty(IKeys.COMMUNICATION);
//        if (communication == null) {
//            communication = "conf/communication.xml";
//            System.setProperty(IKeys.COMMUNICATION, communication);
//			LOG.warn("commmunication is not defined. using default.");
//        }
//        if (!new File(communication).exists()) {
//            LOG.warn("communication (" + communication + ") does not exists. please create one via ui setup.");
//        }

    }

    public void contextDestroyed(final ServletContextEvent contextEvent) {

    }

}
