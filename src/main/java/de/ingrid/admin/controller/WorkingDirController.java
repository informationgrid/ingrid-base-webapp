package de.ingrid.admin.controller;

import java.io.File;

import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.WorkingDirEditor;
import de.ingrid.admin.command.PlugdescriptionCommandObject;

@Controller
@SessionAttributes("plugDescription")
@RequestMapping(value = "/base/workingDir.html")
public class WorkingDirController {

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
            final Errors errors) {
        if (validateDirectory(errors, plugDescription).hasErrors()) {
            return "/base/workingDir";
        }
        // TODO validate
        return "redirect:/base/general.html";
    }

    private final Errors validateDirectory(final Errors errors, final PlugdescriptionCommandObject object) {
        if (null == object.getWorkinDirectory()) {
            errors.rejectValue("workinDirectory", null, "Es muss ein Ordner angegeben werden!");
        }
        return errors;
    }
}
