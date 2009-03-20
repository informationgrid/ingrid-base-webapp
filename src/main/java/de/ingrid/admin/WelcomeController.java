package de.ingrid.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@RequestMapping(value = "/base/welcome.html")
@SessionAttributes("plugDescription")
public class WelcomeController {

    @RequestMapping(method = RequestMethod.GET)
    public String welcome(Model model) {
        model.addAttribute("plugDescription", new PlugdescriptionCommandObject());
        return "base/welcome";
    }
}
