package de.ingrid.admin.controller;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.WorkingDirEditor;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.validation.PlugDescValidator;

@Controller
@SessionAttributes("plugDescription")
@RequestMapping(value = "/base/workingDir.html")
public class WorkingDirController {

    private final PlugDescValidator _validator;

    @Autowired
    public WorkingDirController(final PlugDescValidator validator) {
        _validator = validator;
    }

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(File.class, new WorkingDirEditor());
    }

    @RequestMapping(method = RequestMethod.GET)
    public String getWorkingDir(@ModelAttribute("plugDescription") final PlugdescriptionCommandObject plugDescription) {
        return "/base/workingDir";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String postWorkingDir(@ModelAttribute("plugDescription") final PlugdescriptionCommandObject plugDescription,
            final BindingResult errors) {
        if (_validator.validateWorkingDir(errors).hasErrors()) {
            return "/base/workingDir";
        }
        return "redirect:/base/general.html";
    }
}
