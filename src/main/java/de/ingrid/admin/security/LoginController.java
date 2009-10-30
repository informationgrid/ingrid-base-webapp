package de.ingrid.admin.security;

import java.io.File;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.ingrid.admin.IKeys;

@Controller
public class LoginController {

    @ModelAttribute("securityEnabled")
    public Boolean injectAuthenticate() {
        String pd = System.getProperty(IKeys.PLUG_DESCRIPTION);
        File file = new File(pd);
        return file.exists();
    }

    @RequestMapping(value = "/base/auth/login.html", method = RequestMethod.GET)
    public String login(Model model, HttpSession session) {
        return "/base/login";
    }

    @RequestMapping(value = "/base/auth/loginFailure.html", method = RequestMethod.GET)
    public String loginFailure() {
        return "/base/loginFailure";
    }

    @RequestMapping(value = "/base/auth/roleFailure.html", method = RequestMethod.GET)
    public String roleFailure() {
        return "/base/roleFailure";
    }

    @RequestMapping(value = "/base/auth/logout.html", method = RequestMethod.GET)
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/base/welcome.html";
    }

}
