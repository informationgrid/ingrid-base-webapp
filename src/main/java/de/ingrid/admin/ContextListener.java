package de.ingrid.admin;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

public class ContextListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(ContextListener.class);

    public void contextInitialized(final ServletContextEvent contextEvent) {
        // plug description
        String plugDescription = System.getProperty(IKeys.PLUG_DESCRIPTION);
		if (plugDescription == null) {
            plugDescription = "conf/plugdescription.xml";
            System.setProperty(IKeys.PLUG_DESCRIPTION, plugDescription);
            LOG.warn("plug description is not defined. using default: " + plugDescription);
        }

        // communication
        String communication = System.getProperty(IKeys.COMMUNICATION);
        if (communication == null) {
            communication = "conf/communication.xml";
            System.setProperty(IKeys.COMMUNICATION, communication);
			LOG.warn("commmunication is not defined. using default.");
        }
        if (!new File(communication).exists()) {
            LOG.warn("communication (" + communication + ") does not exists. please create one via ui setup.");
        }

        // indexing
        final String indexing = System.getProperty(IKeys.INDEXING);
        if (null == indexing) {
            System.setProperty(IKeys.INDEXING, "true");
        } else if (indexing.equals("false")) {
            System.clearProperty(IKeys.INDEXING);
        }
    }

    public void contextDestroyed(final ServletContextEvent contextEvent) {

    }

}
