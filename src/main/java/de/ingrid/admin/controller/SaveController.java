package de.ingrid.admin.controller;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.service.CommunicationInterface;
import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.xml.XMLSerializer;

@Controller
@SessionAttributes(value = { "plugDescription" })
@RequestMapping("/base/save.html")
public class SaveController {

    private final CommunicationInterface _communicationInterface;
    private final IConfigurable[] _configurables;

    @Autowired
    public SaveController(final CommunicationInterface communicationInterface, final IConfigurable... configurables) {
        _communicationInterface = communicationInterface;
        _configurables = configurables;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String save() {
        return "/base/save";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String postSave(@ModelAttribute("plugDescription") final PlugdescriptionCommandObject plugdescriptionCommandObject)
            throws Exception {
        savePlugDescription(plugdescriptionCommandObject);
        return "redirect:/base/welcome.html";
    }

    private void savePlugDescription(final PlugdescriptionCommandObject plugdescriptionCommandObject) throws Exception {
        // save
        final PlugDescription plugDescription = new PlugDescription();
        plugDescription.putAll(plugdescriptionCommandObject);
        final XMLSerializer serializer = new XMLSerializer();
        serializer.serialize(plugDescription, new File(System.getProperty("plugDescription")));

        if (System.getProperty("mapping") != null) {
            for (final IConfigurable configurable : _configurables) {
                configurable.configure(plugDescription);
            }
        }
    }
}
