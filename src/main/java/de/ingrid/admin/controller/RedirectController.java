package de.ingrid.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/iplug/welcome.html")
public class RedirectController {

    @RequestMapping(method = RequestMethod.GET)
    public String get() {
        return "redirect:/base/save.html";
    }
}
