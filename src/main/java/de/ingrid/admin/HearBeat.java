package de.ingrid.admin;

import java.io.IOException;
import java.util.TimerTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.utils.IBus;
import de.ingrid.utils.PlugDescription;

@Service
public class HearBeat extends TimerTask {

    private final PlugDescriptionService _plugDescriptionService;
    private final CommunicationInterface _communicationInterface;

    @Autowired
    public HearBeat(PlugDescriptionService plugDescriptionService, CommunicationInterface communicationInterface) {
        _plugDescriptionService = plugDescriptionService;
        _communicationInterface = communicationInterface;
    }

    @Override
    public void run() {
        try {
            IBus bus = _communicationInterface.getIBus();
            PlugDescription plugDescription = _plugDescriptionService.readPlugDescription();
            if (!bus.containsPlugDescription(plugDescription.getPlugId(), plugDescription.getMd5Hash())) {
                bus.addPlugDescription(plugDescription);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ;

    }

}
