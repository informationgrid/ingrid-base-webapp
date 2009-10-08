package de.ingrid.admin.controller;

import java.io.File;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.command.PlugdescriptionCommandObject;

@Controller
@RequestMapping(value = "/base/welcome.html")
@SessionAttributes("plugDescription")
public class WelcomeController {

    private static PlugdescriptionCommandObject _plugDescription;

    @RequestMapping(method = RequestMethod.GET)
    public String welcome(final ModelMap modelMap) throws Exception {
        loadPlugDescription(modelMap);
        return "base/welcome";
    }
    
    //@ModelAttribute("plugDescription")
    //public PlugdescriptionCommandObject load(final ModelMap modelMap) throws Exception {
    //    return WelcomeController.loadPlugDescription(modelMap);
    //}

    public static PlugdescriptionCommandObject loadPlugDescription(final ModelMap modelMap) throws Exception {
        if (_plugDescription == null) {
            _plugDescription = new PlugdescriptionCommandObject(new File(System.getProperty("plugDescription")));
            modelMap.addAttribute("plugDescription", _plugDescription);
        }
        return _plugDescription;
    }
}
