package de.ingrid.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class FinishController {

    public static final String FINISH_URI = "/base/finish.html";

    public static final String FINISH_VIEW = "/base/finish";

    @RequestMapping(value = FINISH_URI, method = RequestMethod.GET)
    public String getFinish() {
        return FINISH_VIEW;
    }
}
