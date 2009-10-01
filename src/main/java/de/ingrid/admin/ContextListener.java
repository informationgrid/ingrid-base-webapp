package de.ingrid.admin;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ContextListener implements ServletContextListener {

    private static final Log LOG = LogFactory.getLog(ContextListener.class);

    public void contextInitialized(final ServletContextEvent arg0) {
        String plugDescription = System.getProperty("plugDescription");
		if (plugDescription == null) {
            // rather doing this, then stopping the system
            plugDescription = "conf/plugDescription.xml";
			System.setProperty("plugDescription", plugDescription);
            // just commenting this out because it's a little bit stressful to
			// type this again and again
			LOG.warn("plug description is not defined. using default.");
        }
        if (!new File(plugDescription).exists()) {
            LOG.warn("plug description (" + plugDescription + ") does not exists. please create one via ui setup.");
        }

        String communication = System.getProperty("communication");
        if (communication == null) {
			// the same here
            communication = "conf/communication.xml";
			System.setProperty("communication", communication);
			LOG.warn("commmunication is not defined. using default.");
        }
        if (!new File(communication).exists()) {
            LOG.warn("communication (" + communication + ") does not exists. please create one via ui setup.");
        }

        final String mapping = System.getProperty("mapping");
        if (null == mapping) {
            System.setProperty("mapping", "true");
        } else if (mapping.equals("false")) {
            System.clearProperty("mapping");
        }

        final String indexing = System.getProperty("indexing");
        if (null == indexing) {
            System.setProperty("indexing", "true");
        } else if (indexing.equals("false")) {
            System.clearProperty("indexing");
        }
    }

    public void contextDestroyed(final ServletContextEvent contextEvent) {

    }

}
