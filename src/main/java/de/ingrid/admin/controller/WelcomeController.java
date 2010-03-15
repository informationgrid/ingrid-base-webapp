package de.ingrid.admin.controller;

import java.io.File;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.IKeys;
import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.service.PlugDescriptionService;

@Controller
@SessionAttributes("plugDescription")
public class WelcomeController {

    private final PlugDescriptionService _service;
    
    @Autowired
    public WelcomeController(final PlugDescriptionService service) {
        _service = service;
    }

    @RequestMapping(value = IUris.WELCOME, method = RequestMethod.GET)
    public String welcome(final HttpSession session) throws Exception {
        if (session.getAttribute(IKeys.PLUG_DESCRIPTION) == null) {
            // create a new PlugDescription only here when trying to be accessed via admin page
            // and not during initialisation of iPlug (otherwise errors for empty file will occur)
            if (_service.existsPlugDescription())
                session.setAttribute(IKeys.PLUG_DESCRIPTION, _service.getPlugDescription());
            else
                session.setAttribute(IKeys.PLUG_DESCRIPTION, new PlugdescriptionCommandObject(new File(System.getProperty("plugDescription"))));
        }
        return IViews.WELCOME;
    }
}