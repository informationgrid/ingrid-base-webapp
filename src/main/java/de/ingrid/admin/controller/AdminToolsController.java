package de.ingrid.admin.controller;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.ingrid.admin.service.CommunicationService;
import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.utils.xml.PlugdescriptionSerializer;

@Controller
public class AdminToolsController {

    public static final String COMM_SETUP_URI = "/base/commSetup.html";

    public static final String COMM_SETUP_VIEW = "/base/commSetup";

    public static final String HEARTBEAT_SETUP_URI = "/base/heartbeat.html";

    public static final String HEARTBEAT_SETUP_VIEW = "/base/heartbeat";

    private final CommunicationService _communication;

    private final HeartBeatPlug _plug;

    @Autowired
    public AdminToolsController(final CommunicationService communication, final HeartBeatPlug plug) throws Exception {
        _communication = communication;

        _plug = plug;
        final File file = new File(System.getProperty("plugDescription"));
        if (file.exists()) {
            final PlugdescriptionSerializer serializer = new PlugdescriptionSerializer();
            _plug.configure(serializer.deSerialize(file));
        }
    }

    @RequestMapping(value = COMM_SETUP_URI, method = RequestMethod.GET)
    public String getCommSetup(final ModelMap modelMap) {
        modelMap.addAttribute("connected", _communication.isConnected());
        return COMM_SETUP_VIEW;
    }

    @RequestMapping(value = COMM_SETUP_URI, method = RequestMethod.POST)
    public String postCommSetup(final ModelMap modelMap, @RequestParam("action") final String action) throws Exception {
        if ("shutdown".equals(action)) {
            _communication.shutdown();
        } else if ("restart".equals(action)) {
            _communication.restart();
        } else if ("start".equals(action)) {
            _communication.start();
        }
        return getCommSetup(modelMap);
    }

    @RequestMapping(value = HEARTBEAT_SETUP_URI, method = RequestMethod.GET)
    public String getHeartbeat(final ModelMap modelMap) {
        modelMap.addAttribute("enabled", _plug.sendingHeartBeats());
        return HEARTBEAT_SETUP_VIEW;
    }

    @RequestMapping(value = HEARTBEAT_SETUP_URI, method = RequestMethod.POST)
    public String setHeartBeat(final ModelMap modelMap, @RequestParam("action") final String action) throws IOException {
        if ("start".equals(action)) {
            _plug.startHeartBeats();
        } else if ("stop".equals(action)) {
            _plug.stopHeartBeats();
        }
        return getHeartbeat(modelMap);
    }
}
