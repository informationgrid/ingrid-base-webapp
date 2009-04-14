package de.ingrid.admin;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextListener implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(ContextListener.class);

    public void contextInitialized(ServletContextEvent arg0) {
        String plugDescription = System.getProperty("plugDescription");
        if (plugDescription == null) {
            throw new RuntimeException("no plugdescription defined!");
        }
        if (!new File(plugDescription).exists()) {
            LOG.warn("plugdescription does not exists. create one via ui setup. " + plugDescription);
        }

        String communication = System.getProperty("communication");
        if (communication == null) {
            throw new RuntimeException("no communication defined!");
        }
        if (!new File(communication).exists()) {
            LOG.warn("communication.xml does not exists. create one via ui setup. " + communication);
        }
    }

    public void contextDestroyed(ServletContextEvent contextEvent) {

    }

}
