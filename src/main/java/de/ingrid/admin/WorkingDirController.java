package de.ingrid.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@SessionAttributes("plugDescription")
@RequestMapping(value = "/base/workingDir.html")
public class WorkingDirController {

    @RequestMapping(method = RequestMethod.GET)
    public String getWorkingDir(@ModelAttribute("plugDescription") PlugdescriptionCommandObject commandObject) {
        return "/base/workingDir";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String postWorkingDir(@ModelAttribute("plugDescription") PlugdescriptionCommandObject plugdescription) {
        // TODO validate
        return "redirect:/base/general.html";
    }

}
