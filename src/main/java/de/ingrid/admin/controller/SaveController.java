package de.ingrid.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.service.PlugDescriptionService;
import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.IRecordLoader;

@Controller
@SessionAttributes("plugDescription")
public class SaveController {

    public static final String SAVE_URI = "/base/save.html";

    public static final String SAVE_VIEW = "/base/save";

    private final IConfigurable[] _configurables;

    private final HeartBeatPlug _plug;

    @Autowired
    public SaveController(final HeartBeatPlug plug, final IConfigurable... configurables) {
        _plug = plug;
        _configurables = configurables;
    }

    @RequestMapping(value = SAVE_URI, method = RequestMethod.GET)
    public String save() {
        return SAVE_VIEW;
    }

    @RequestMapping(value = SAVE_URI, method = RequestMethod.POST)
    public String postSave(
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject plugdescriptionCommandObject,
            final PlugDescriptionService plugDescriptionService)
            throws Exception {

        // set class and record loader
        plugdescriptionCommandObject.setIPlugClass(_plug.getClass().getName());
        plugdescriptionCommandObject.setRecordLoader(_plug instanceof IRecordLoader);

        // save plug description
        plugDescriptionService.savePlugDescription(plugdescriptionCommandObject);

        // reconfigure all configurables
        for (final IConfigurable configurable : _configurables) {
            configurable.configure(plugDescriptionService.getPlugDescription());
        }

        // start heart beat
        _plug.startHeartBeats();

        return "redirect:" + SchedulingController.SCHEDULING_URI;
    }
}
