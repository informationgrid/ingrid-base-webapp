package de.ingrid.admin.controller;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.utils.xml.PlugdescriptionSerializer;

@Controller
@RequestMapping(value = "/base/heartbeat.html")
public class HeartBeatController {

    private final HeartBeatPlug _plug;

    @Autowired
    public HeartBeatController(final HeartBeatPlug plug) throws Exception {
        _plug = plug;
        final File file = new File(System.getProperty("plugDescription"));
        if (file.exists()) {
            final PlugdescriptionSerializer serializer = new PlugdescriptionSerializer();
            _plug.configure(serializer.deSerialize(file));
        }
    }

    @ModelAttribute("enabled")
    public boolean sendingHeartBeats() {
        return _plug.sendingHeartBeats();
    }

    @RequestMapping(method = RequestMethod.GET)
    public String heartbeat() {
        return "/base/heartbeat";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String handleHeartbeat(@RequestParam("action") final String action) throws IOException {
        if ("start".equals(action)) {
            _plug.startHeartBeats();
        } else if ("stop".equals(action)) {
            _plug.stopHeartBeats();
        }
        return "redirect:/base/heartbeat.html";
    }

}
