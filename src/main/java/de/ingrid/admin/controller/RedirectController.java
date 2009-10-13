package de.ingrid.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@SessionAttributes("plugDescription")
public class RedirectController {

    public static final String IPLUG_WELCOME_URI = "/iplug/welcome.html";

    @RequestMapping(value = IPLUG_WELCOME_URI, method = RequestMethod.GET)
    public String get() {
        return "redirect:" + SaveController.SAVE_URI;
    }
}
