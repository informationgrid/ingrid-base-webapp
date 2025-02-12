/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.admin.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.IKeys;
import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;
import de.ingrid.admin.command.Command;
import de.ingrid.admin.service.PlugDescriptionService;

import java.io.IOException;

@Controller
@SessionAttributes({"plugDescription", "postCommandObject"})
public class WelcomeController {

    private final PlugDescriptionService _service;

    @Autowired
    public WelcomeController(final PlugDescriptionService service) {
        _service = service;
    }

    @GetMapping("/")
    public String root() throws IOException {
        return IKeys.REDIRECT + IUris.WELCOME;
    }

    @RequestMapping(value = IUris.WELCOME, method = RequestMethod.GET)
    public String welcome(final HttpSession session) throws Exception {
        if (session.getAttribute(IKeys.PLUG_DESCRIPTION) == null) {
            session.setAttribute(IKeys.PLUG_DESCRIPTION, _service.getCommandObect());
        }
        if (session.getAttribute("postCommandObject") == null) {
            session.setAttribute("postCommandObject", new Command());
        }

        String redirectUrl = (String) session.getAttribute("redirectUrl");

        if (redirectUrl != null && (redirectUrl.contains( "base" ) || redirectUrl.contains( "iplug-pages" ) )) {
            session.removeAttribute( "redirectUrl" );
            return IKeys.REDIRECT + redirectUrl;
        } else {
            return IViews.WELCOME;
        }
    }
}
