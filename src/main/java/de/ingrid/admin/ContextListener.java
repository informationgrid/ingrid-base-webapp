package de.ingrid.admin;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ContextListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent arg0) {
        String plugDescription = System.getProperty("plugDescription");
        if (plugDescription == null) {
            throw new RuntimeException("no plugdescription defined!");
        }

        String communication = System.getProperty("communication");
        if (communication == null) {
            throw new RuntimeException("no communication defined!");
        }
    }

    public void contextDestroyed(ServletContextEvent contextEvent) {

    }

}
