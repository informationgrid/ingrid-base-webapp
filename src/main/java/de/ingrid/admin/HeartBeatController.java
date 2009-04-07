package de.ingrid.admin;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value = "/base/heartbeat.html")
public class HeartBeatController {

    private final HeartBeat _heartBeat;

    @Autowired
    public HeartBeatController(HeartBeat heartBeat) {
        _heartBeat = heartBeat;
    }

    @ModelAttribute("heartBeat")
    public HeartBeat injectHeartBeat() {
        return _heartBeat;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String heartbeat() {
        return "/base/heartbeat";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String handleHeartbeat(@RequestParam("start") boolean start) throws IOException {
        if (start) {
            _heartBeat.enable();
        } else {
            _heartBeat.disable();
        }
        return "redirect:/base/heartbeat.html";
    }

}
