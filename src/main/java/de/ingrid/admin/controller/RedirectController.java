package de.ingrid.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.IUris;

@Controller
@SessionAttributes("plugDescription")
public class RedirectController extends AbstractController {

    @RequestMapping(value = IUris.IPLUG_WELCOME, method = RequestMethod.GET)
    public String get() {
        return redirect(IUris.SAVE);
    }
}
