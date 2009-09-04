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
			// rather doing this, then stopping the sysytem
            plugDescription = "conf/plugDescription.xml";
			System.setProperty("plugDescription", plugDescription);
			// just commenting this out because it's a lil bit stressfull to
			// type this again and again
			// throw new RuntimeException("no plugdescription defined!");
			// okay lets show a warning
			LOG.warn("plug description is not defined. using default.");
        }
        if (!new File(plugDescription).exists()) {
            LOG.warn("plugdescription does not exists. create one via ui setup. " + plugDescription);
        }

        String communication = System.getProperty("communication");
        if (communication == null) {
			// the same here
            communication = "conf/communication.xml";
			System.setProperty("communication", communication);
			// throw new RuntimeException("no communication defined!");
			LOG.warn("commmunication is not defined. using default.");
        }
        if (!new File(communication).exists()) {
            LOG.warn("communication.xml does not exists. create one via ui setup. " + communication);
        }
    }

    public void contextDestroyed(final ServletContextEvent contextEvent) {

    }

}
