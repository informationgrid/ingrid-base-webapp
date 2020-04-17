/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.admin.security;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.ingrid.admin.service.PlugDescriptionService;

@Controller
public class LoginController {

    private final PlugDescriptionService _plugDescriptionService;

    @Autowired
    public LoginController(final PlugDescriptionService plugDescriptionService) {
        _plugDescriptionService = plugDescriptionService;
    }

    @ModelAttribute("securityEnabled")
    public Boolean injectAuthenticate() {
        return _plugDescriptionService.isIPlugSecured();
    }

    @RequestMapping(value = "/base/auth/login.html", method = RequestMethod.GET)
    public String login(final Model model, final HttpSession session) {
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
    public String logout(final HttpSession session) {
        session.invalidate();
        return "redirect:/base/welcome.html";
    }

}
