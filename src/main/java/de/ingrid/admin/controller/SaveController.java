package de.ingrid.admin.controller;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.xml.XMLSerializer;

@Controller
@SessionAttributes("plugDescription")
@RequestMapping("/base/save.html")
public class SaveController {

    private final IConfigurable[] _configurables;

    private final HeartBeatPlug _plug;

    @Autowired
    public SaveController(final HeartBeatPlug plug, final IConfigurable... configurables) {
        _plug = plug;
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
        // save plug class
        plugdescriptionCommandObject.setIPlugClass(_plug.getClass().getName());
        plugdescriptionCommandObject.setRecordLoader(_plug.isRecordLoader());
        // save
        final PlugDescription plugDescription = new PlugDescription();
        plugDescription.putAll(plugdescriptionCommandObject);
        final XMLSerializer serializer = new XMLSerializer();
        serializer.serialize(plugDescription, new File(System.getProperty("plugDescription")));

        for (final IConfigurable configurable : _configurables) {
            configurable.configure(plugDescription);
        }
    }
}
