/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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

import de.ingrid.admin.Config;
import org.apache.log4j.Logger;

import de.ingrid.admin.IUris;
import de.ingrid.admin.JettyStarter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

@Service("StepFilter")
public class StepFilter implements Filter {

    private File _plugDescription;

    private File _communication;

    private final List<String> _needComm = new ArrayList<>();

    private final List<String> _needPlug = new ArrayList<>();

    protected final Logger LOG = Logger.getLogger(StepFilter.class);

    private final Config config;

    @Autowired
    public StepFilter(Config config) throws ServletException {
        this.config = config;
        this.init(null);
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {

        _plugDescription = new File(config.getPlugdescription());
        _communication = new File(config.communicationLocation);

        boolean iBusDisabled = config.disableIBus;
        if (iBusDisabled) return;

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
        _needComm.add(IUris.CACHING);

        _needPlug.add(IUris.SCHEDULING);
        _needPlug.add(IUris.INDEXING);
        _needPlug.add(IUris.FINISH);
        _needPlug.add(IUris.HEARTBEAT_SETUP);
        _needPlug.add(IUris.SEARCH);
        _needPlug.add(IUris.SEARCH_DETAILS);
        _needPlug.add(IUris.CACHING);
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

        req.setAttribute("communicationExists", _communication.exists());
        req.setAttribute("plugdescriptionExists", _plugDescription.exists());

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
