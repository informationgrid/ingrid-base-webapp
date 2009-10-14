package de.ingrid.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.service.PlugDescriptionService;
import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.IRecordLoader;

@Controller
@SessionAttributes("plugDescription")
public class SaveController extends AbstractController {

    private final IConfigurable[] _configurables;

    private final HeartBeatPlug _plug;

    private final PlugDescriptionService _plugDescriptionService;

    @Autowired
    public SaveController(final HeartBeatPlug plug, final PlugDescriptionService plugDescriptionService,
            final IConfigurable... configurables) {
        _plug = plug;
        _plugDescriptionService = plugDescriptionService;
        _configurables = configurables;
    }

    @RequestMapping(value = IUris.SAVE, method = RequestMethod.GET)
    public String save() {
        return IViews.SAVE;
    }

    @RequestMapping(value = IUris.SAVE, method = RequestMethod.POST)
    public String postSave(
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject plugdescriptionCommandObject)
            throws Exception {

        // set class and record loader
        plugdescriptionCommandObject.setIPlugClass(_plug.getClass().getName());
        plugdescriptionCommandObject.setRecordLoader(_plug instanceof IRecordLoader);

        // save plug description
        _plugDescriptionService.savePlugDescription(plugdescriptionCommandObject);

        // reconfigure all configurables
        for (final IConfigurable configurable : _configurables) {
            configurable.configure(_plugDescriptionService.getPlugDescription());
        }

        // start heart beat
        _plug.startHeartBeats();

        return redirect(IUris.SCHEDULING);
    }
}
