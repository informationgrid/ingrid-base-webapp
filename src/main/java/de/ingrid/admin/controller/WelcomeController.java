package de.ingrid.admin.controller;

import java.io.File;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;
import de.ingrid.admin.command.PlugdescriptionCommandObject;

@Controller
@SessionAttributes("plugDescription")
public class WelcomeController {

    private static PlugdescriptionCommandObject _plugDescription;

    @RequestMapping(value = IUris.WELCOME, method = RequestMethod.GET)
    public String welcome(final ModelMap modelMap) throws Exception {
        loadPlugDescription(modelMap);
        return IViews.WELCOME;
    }

    public PlugdescriptionCommandObject loadPlugDescription(final ModelMap modelMap) throws Exception {
        if (_plugDescription == null) {
            _plugDescription = new PlugdescriptionCommandObject(new File(System.getProperty("plugDescription")));
            modelMap.addAttribute("plugDescription", _plugDescription);
        }
        return _plugDescription;
    }
}