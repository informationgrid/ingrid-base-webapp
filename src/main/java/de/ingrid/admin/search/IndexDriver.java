package de.ingrid.admin.search;

import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.HashSessionIdManager;
import org.mortbay.jetty.webapp.WebAppContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import de.ingrid.admin.search.IndexRunnable;

/**
 * This is a indexer driver to (re) create the index of the the iPlugs. It is
 * typically called from the index.sh script (see in iPlugs for detail).
 * 
 * Since we use heavy spring configuration, the jetty servlet container is
 * started to resolve the spring dependency nightmare.
 * 
 * @author joachim@wemove.com
 * 
 */
public class IndexDriver {

    private static final Log log = LogFactory.getLog(IndexDriver.class);

    private static String DEFAULT_WEBAPP_DIR = "webapp";

    private static int DEFAULT_JETTY_PORT = 8082;

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        if (!System.getProperties().containsKey("jetty.webapp"))
            log.warn("Property 'jetty.webapp' not defined! Using default webapp directory, which is '"
                    + DEFAULT_WEBAPP_DIR + "'.");
        if (!System.getProperties().containsKey("jetty.port"))
            log.warn("Property 'jetty.port' not defined! Using default port, which is '" + DEFAULT_JETTY_PORT + "'.");

        WebAppContext webAppContext = new WebAppContext(System.getProperty("jetty.webapp", DEFAULT_WEBAPP_DIR), "/");

        Server server = new Server(Integer.getInteger("jetty.port", DEFAULT_JETTY_PORT));
        // fix slow startup time on virtual machine env.
        HashSessionIdManager hsim = new HashSessionIdManager();
        hsim.setRandom(new Random());
        server.setSessionIdManager(hsim);
        server.setHandler(webAppContext);
        server.start();
        WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(webAppContext
                .getServletContext(), "org.springframework.web.servlet.FrameworkServlet.CONTEXT.springapp");
        IndexRunnable r = (IndexRunnable) wac.getBean("indexRunnable");
        r.run();
        server.stop();
    }

}
