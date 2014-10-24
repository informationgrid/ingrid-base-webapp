package de.ingrid.admin.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.IKeys;
import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;
import de.ingrid.admin.command.Command;
import de.ingrid.admin.service.PlugDescriptionService;

@Controller
@SessionAttributes({"plugDescription", "postCommandObject"})
public class WelcomeController {

    private final PlugDescriptionService _service;
    
    @Autowired
    public WelcomeController(final PlugDescriptionService service) {
        _service = service;
    }

    @RequestMapping(value = IUris.WELCOME, method = RequestMethod.GET)
    public String welcome(final HttpSession session) throws Exception {
        if (session.getAttribute(IKeys.PLUG_DESCRIPTION) == null) {
            session.setAttribute(IKeys.PLUG_DESCRIPTION, _service.getCommandObect());
        }
        if (session.getAttribute("postCommandObject") == null) {
            session.setAttribute("postCommandObject", new Command());
        }
        
        String redirectUrl = (String) session.getAttribute("redirectUrl"); 
        
        if (redirectUrl != null && (redirectUrl.contains( "base" ) || redirectUrl.contains( "iplug-pages" ) )) {
            session.removeAttribute( "redirectUrl" );
            return IKeys.REDIRECT + redirectUrl;
        } else {
            return IViews.WELCOME;
        }
    }
}