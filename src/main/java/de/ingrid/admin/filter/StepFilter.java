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

import de.ingrid.admin.IUris;

public class StepFilter implements Filter {

    private File _plugDescription;

    private File _communication;

    private final List<String> _needComm = new ArrayList<String>();

    private final List<String> _needPlug = new ArrayList<String>();

    protected final Logger LOG = Logger.getLogger(StepFilter.class);

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        _plugDescription = new File(System.getProperty("plugDescription"));
        _communication = new File(System.getProperty("communication"));

        _needComm.add(IUris.WORKING_DIR);
        _needComm.add(IUris.GENERAL);
        _needComm.add(IUris.PARTNER);
        _needComm.add(IUris.PROVIDER);
        _needComm.add(IUris.FIELD_QUERY);
        _needComm.add(IUris.IPLUG_WELCOME);
        _needComm.add(IUris.SAVE);
        _needComm.add(IUris.COMM_SETUP);

        _needComm.add(IUris.SCHEDULING);
        _needComm.add(IUris.INDEXING);
        _needComm.add(IUris.FINISH);
        _needComm.add(IUris.HEARTBEAT_SETUP);
        _needComm.add(IUris.SEARCH);
        _needComm.add(IUris.SEARCH_DETAILS);

        _needPlug.add(IUris.SCHEDULING);
        _needPlug.add(IUris.INDEXING);
        _needPlug.add(IUris.FINISH);
        _needPlug.add(IUris.HEARTBEAT_SETUP);
        _needPlug.add(IUris.SEARCH);
        _needPlug.add(IUris.SEARCH_DETAILS);
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest req = (HttpServletRequest) request;
        final HttpServletResponse res = (HttpServletResponse) response;
        final String uri = req.getRequestURI();

        if (!_communication.exists()) {
            req.setAttribute("communicationClickable", "false");
        }
        if (!_plugDescription.exists()) {
            req.setAttribute("plugdescriptionClickable", "false");
        }

        if (!_communication.exists() && _needComm.contains(uri)) {
            LOG.info("communication does not exist but is necessary. redirect to communication setup...");
            res.sendRedirect(IUris.COMMUNICATION);
            return;
        } else if (!_plugDescription.exists() && _needPlug.contains(uri)) {
            LOG.info("plug description does not exist but is necessary. redirect to plug description setup...");
            res.sendRedirect(IUris.WORKING_DIR);
            return;
        }

        chain.doFilter(request, response);
    }

}
