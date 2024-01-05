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

import java.io.File;

import de.ingrid.admin.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.validation.PlugDescValidator;

@Controller
@SessionAttributes("plugDescription")
public class WorkingDirController extends AbstractController {

    private final PlugDescValidator _validator;

    private final Config config;

    @Autowired
    public WorkingDirController(final PlugDescValidator validator, Config config) {
        _validator = validator;
        this.config = config;
    }

    @InitBinder
    public void initBinder(final WebDataBinder binder) throws Exception {
        binder.registerCustomEditor(File.class, new WorkingDirEditor());
    }

    @RequestMapping(value = IUris.WORKING_DIR, method = RequestMethod.GET)
    public String getWorkingDir(@ModelAttribute("plugDescription") final PlugdescriptionCommandObject plugDescription) {
        plugDescription.setRealWorkingDir( config.pdWorkingDir );
        return IViews.WORKING_DIR;
    }

    @RequestMapping(value = IUris.WORKING_DIR, method = RequestMethod.POST)
    public String postWorkingDir(@ModelAttribute("plugDescription") final PlugdescriptionCommandObject plugDescription, final BindingResult errors) {
        if (_validator.validateWorkingDir(errors).hasErrors()) {
            return IViews.WORKING_DIR;
        }
        plugDescription.getWorkinDirectory().mkdirs();
        config.pdWorkingDir = plugDescription.getRealWorkingDir();
        return redirect(IUris.GENERAL);
    }
}
