package de.ingrid.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.IKeys;
import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;
import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.command.Command;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.service.PlugDescriptionService;
import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.IRecordLoader;
import de.ingrid.utils.tool.PlugDescriptionUtil;
import de.ingrid.utils.tool.QueryUtil;

@Controller
@SessionAttributes({"plugDescription", "postCommandObject"})
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
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject plugdescriptionCommandObject,
            @ModelAttribute("postCommandObject") final Command postCommandObject)
            throws Exception {
        
        boolean restart = false;
        
        PlugDescriptionUtil.addFieldToPlugDescription(plugdescriptionCommandObject, QueryUtil.FIELDNAME_INCL_META);

        // set class and record loader
        plugdescriptionCommandObject.setIPlugClass(_plug.getClass().getName());
        plugdescriptionCommandObject.setRecordLoader(_plug instanceof IRecordLoader);

     // if port has changed show a message to the user to restart the iPlug
        if (plugdescriptionCommandObject.containsKey("originalPort")) {
            if (plugdescriptionCommandObject.getIplugAdminGuiPort() != plugdescriptionCommandObject.getInt("originalPort")) {
                restart = true;
            }
            // remove entry from PlugDescription again
            plugdescriptionCommandObject.remove("originalPort");
        }
        
//        if (JettyStarter.getInstance().getExternalConfig() != null) {
//            JettyStarter.getInstance().getExternalConfig().addPlugdescriptionValues( plugdescriptionCommandObject );
//        }
        
        JettyStarter.getInstance().config.writePlugdescriptionToProperties( plugdescriptionCommandObject );
        
        // execute additional command objects
        if(postCommandObject != null){
        	postCommandObject.execute();
        	postCommandObject.clear();
        }

        // redirect to the restart page
        if (restart)
            return redirect(IUris.RESTART);
        
        // reconfigure all configurables
        for (final IConfigurable configurable : _configurables) {
            configurable.configure(plugdescriptionCommandObject);
        }

        // save plug description
        _plugDescriptionService.savePlugDescription(plugdescriptionCommandObject);
        
        // start heart beat
        _plug.startHeartBeats();

        if (System.getProperty(IKeys.INDEXING) != null) {
            return redirect(IUris.SCHEDULING);
        } else {
            return redirect(IUris.FINISH);
        }
    }
}
