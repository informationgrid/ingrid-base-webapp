package de.ingrid.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.ingrid.utils.IBus;
import de.ingrid.utils.IPlug;
import de.ingrid.utils.PlugDescription;

@Controller
public class IPlugController {

    private final CommunicationInterface _communicationInterface;
    private final WrappedIPlug _wrappedIPlug;
    private final PlugDescriptionService _plugDescriptionService;

    @Autowired
    public IPlugController(CommunicationInterface communicationInterface, WrappedIPlug wrappedIPlug,
            PlugDescriptionService plugDescriptionService) {
        _communicationInterface = communicationInterface;
        _wrappedIPlug = wrappedIPlug;
        _plugDescriptionService = plugDescriptionService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public void postStartIPlug() throws Exception {
        IBus bus = _communicationInterface.getIBus();
        IPlug plug = _wrappedIPlug.getPlug();
        PlugDescription plugDescription = _plugDescriptionService.readPlugDescription();
        plug.configure(plugDescription);
        bus.addPlugDescription(plugDescription);
    }
}
