package de.ingrid.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;

@Controller
public class WelcomeController {

    @RequestMapping(value = IUris.WELCOME, method = RequestMethod.GET)
    public String welcome(final ModelMap modelMap) throws Exception {
        return IViews.WELCOME;
    }

}
