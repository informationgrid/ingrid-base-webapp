package de.ingrid.admin;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.utils.IBus;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.tool.MD5Util;

@Service
public class HeartBeat extends TimerTask {

    private final PlugDescriptionService _plugDescriptionService;
    private final CommunicationInterface _communicationInterface;
    private Timer _timer;
    private boolean _enable = false;
    private static final Logger LOG = LoggerFactory.getLogger(HeartBeat.class);

    @Autowired
    public HeartBeat(PlugDescriptionService plugDescriptionService, CommunicationInterface communicationInterface) {
        _plugDescriptionService = plugDescriptionService;
        _communicationInterface = communicationInterface;
        _timer = new Timer(true);
        _timer.schedule(this, new Date(), 1000 * 60);
    }

    public void enable() throws IOException {
        _enable = true;
    }

    public void disable() {
        _enable = false;
    }

    public boolean isBeatable() {
        return _plugDescriptionService.existsPlugdescription() && _communicationInterface.getIBus() != null;
    }

    public boolean isEnable() {
        return _enable;
    }

    @Override
    public void run() {
        if (_enable) {
            LOG.info("send heartbeat");
            try {
                PlugDescription plugDescription = _plugDescriptionService.readHeartBeatPlugDescription();
                File plugdescriptionAsFile = _plugDescriptionService.getPlugDescriptionAsFile();
                IBus bus = _communicationInterface.getIBus();
                String md5 = MD5Util.getMD5(plugdescriptionAsFile);
                plugDescription.setMd5Hash(md5);
                if (!bus.containsPlugDescription(plugDescription.getPlugId(), plugDescription.getMd5Hash())) {
                    bus.addPlugDescription(plugDescription);
                }
            } catch (Throwable e) {
                LOG.error("can not send heartbeat", e);
            }
        } else {
            LOG.info("heartbeat is disabled");
        }

    }

}
