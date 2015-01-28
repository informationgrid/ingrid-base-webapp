/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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
package de.ingrid.admin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class SimpleForwardServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(SimpleForwardServlet.class);

    private static final long serialVersionUID = 2638423380525056495L;

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final String servletPath = req.getServletPath();
        final String realPath = getServletContext().getRealPath(servletPath);
        final File file = new File(realPath);
        if (!file.exists()) {
            LOG.debug("file not found: " + file.getAbsolutePath());
            resp.sendError(404);
            return;
        }
        LOG.debug("load file: " + file.getAbsolutePath());
        if (file.getName().endsWith( ".css" )) {
            resp.setContentType( "text/css" );
        }
        final FileInputStream fileInputStream = new FileInputStream(file);
        final ServletOutputStream outputStream = resp.getOutputStream();
        int read = -1;
        final byte[] buffer = new byte[1024];
        while ((read = fileInputStream.read(buffer)) > -1) {
            outputStream.write(buffer, 0, read);
        }
        fileInputStream.close();
        outputStream.flush();
    }
}
