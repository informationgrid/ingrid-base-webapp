package de.ingrid.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.command.PlugdescriptionCommandObject;

@Controller
@SessionAttributes("plugDescription")
@RequestMapping(value = "/base/workingDir.html")
public class WorkingDirController {

    @RequestMapping(method = RequestMethod.GET)
    public String getWorkingDir(@ModelAttribute("plugDescription") PlugdescriptionCommandObject plugDescription) {
        return "/base/workingDir";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String postWorkingDir(@ModelAttribute("plugDescription") PlugdescriptionCommandObject plugDescription) {
        // TODO validate
        return "redirect:/base/general.html";
    }

}
