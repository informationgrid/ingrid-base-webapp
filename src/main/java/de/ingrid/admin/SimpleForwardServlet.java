package de.ingrid.admin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleForwardServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleForwardServlet.class);

    private static final long serialVersionUID = 2638423380525056495L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();
        String realPath = getServletContext().getRealPath(servletPath);
        File file = new File(realPath);
        if (!file.exists()) {
            LOG.warn("file not found: " + file.getAbsolutePath());
            resp.sendError(404);
            return;
        }
        LOG.trace("load file: " + file.getAbsolutePath());
        FileInputStream fileInputStream = new FileInputStream(file);
        ServletOutputStream outputStream = resp.getOutputStream();
        int read = -1;
        byte[] buffer = new byte[1024];
        while ((read = fileInputStream.read(buffer)) > -1) {
            outputStream.write(buffer, 0, read);
        }
        fileInputStream.close();
        outputStream.flush();
    }
}
