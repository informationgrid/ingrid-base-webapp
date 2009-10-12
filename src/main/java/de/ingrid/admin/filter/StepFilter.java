package de.ingrid.admin.filter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class StepFilter implements Filter {

    private File _plugDescription;

    private File _communication;

    private final String _stepHome = "/base/welcome.html";

    private final String _stepOne = "/base/communication.html";

    private final String _stepTwo = "/base/workingDir.html";

    private final List<String> _stepThree = new ArrayList<String>();

    private final List<String> _stepTools = new ArrayList<String>();

    protected final Logger LOG = Logger.getLogger(StepFilter.class);

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        _plugDescription = new File(System.getProperty("plugDescription"));
        _communication = new File(System.getProperty("communication"));

        _stepThree.add("/base/scheduling.html");
        _stepThree.add("/base/indexing.html");

        _stepTools.add("/base/commSetup.html");
        _stepTools.add("/base/heartbeat.html");
        _stepTools.add("/base/search.html");
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        final HttpServletRequest req = (HttpServletRequest) request;
        final HttpServletResponse res = (HttpServletResponse) response;
        final String uri = req.getRequestURI();

        // only check if uri is not home or step one
        if (!_stepHome.equals(uri) && !_stepOne.equals(uri)) {
            if (!_communication.exists()) {
                // if communication does not exist it must be created
                LOG.info("communication does not exist but is necessary. redirect to communication setup...");
                res.sendRedirect(_stepOne);
                return;
            } else if (!_plugDescription.exists()
                    && (_stepThree.contains(uri) || _stepTools.get(1).equals(uri) || _stepTools.get(2).equals(uri))) {
                // if plug description does not exist and request is one of step
                // three or a specific admin tool it must be created
                LOG.info("plug description does not exist but is necessary. redirect to plug description setup...");
                res.sendRedirect(_stepTwo);
                return;
            }
        }

        // everything is ok, do filter
        chain.doFilter(request, response);
    }

}
