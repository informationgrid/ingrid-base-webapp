package de.ingrid.admin;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

public class ContextListener implements ServletContextListener {

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
