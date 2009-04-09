package de.ingrid.admin;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

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
    public SaveController(CommunicationInterface communicationInterface, IConfigurable... configurables) {
        _communicationInterface = communicationInterface;
        _configurables = configurables;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String save() {
        return "/base/save";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String postSave(@ModelAttribute("plugDescription") PlugdescriptionCommandObject plugdescriptionCommandObject)
            throws IOException {

        File workinDirectory = plugdescriptionCommandObject.getWorkinDirectory();
        PlugDescription plugDescription = new PlugDescription();

        // // working dir
        // plugDescription.setWorkinDirectory(workinDirectory);
        //
        // // partner & provider
        // String[] partners = plugdescriptionCommandObject.getPartners();
        // for (String partner : partners) {
        // plugDescription.addPartner(partner);
        // }
        // String[] providers = plugdescriptionCommandObject.getProviders();
        // for (String provider : providers) {
        // plugDescription.addProvider(provider);
        // }

        // name
        plugDescription.setPlugId(_communicationInterface.getPeerName());
        plugDescription.setProxyServiceURL(_communicationInterface.getPeerName());

        plugDescription.putAll(plugdescriptionCommandObject);
        // save
        String plugDescriptionFile = System.getProperty("plugDescription");
        workinDirectory.mkdirs();
        XMLSerializer serializer = new XMLSerializer();
        serializer.serialize(plugDescription, new File(plugDescriptionFile));

        for (IConfigurable configurable : _configurables) {
            configurable.configure(plugDescription);
        }
        return "redirect:/base/welcome.html";
    }
}
