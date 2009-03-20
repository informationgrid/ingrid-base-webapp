package de.ingrid.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@RequestMapping("/base/finishBase.html")
@SessionAttributes("plugDescription")
public class FinishBaseController {
    
    @RequestMapping(method = RequestMethod.GET)
    public String getFinish() {
        return "/base/finishBase";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String postFinish() {
        return "redirect:/iplug/welcome.html";
    }
}
