package de.ingrid.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.ingrid.admin.service.CommunicationInterface;

@Controller
public class AdminToolsController {

    public static final String BASE_COMM_SETUP_URI = "/base/commSetup.html";

    public static final String BASE_COMM_SETUP_VIEW = "/base/commSetup";

    private final CommunicationInterface _communication;

    @Autowired
    public AdminToolsController(final CommunicationInterface communication) {
        _communication = communication;
    }

    @RequestMapping(value = BASE_COMM_SETUP_URI, method = RequestMethod.GET)
    public String getCommSetup(final ModelMap modelMap) {
        modelMap.addAttribute("connected", _communication.isConnected());
        return BASE_COMM_SETUP_VIEW;
    }

    @RequestMapping(value = BASE_COMM_SETUP_URI, method = RequestMethod.POST)
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
}
