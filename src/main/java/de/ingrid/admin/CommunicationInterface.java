package de.ingrid.admin;

import java.io.File;
import java.io.FileInputStream;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication.tcp.StartCommunication;

import org.springframework.stereotype.Service;

import de.ingrid.ibus.client.BusClientFactory;
import de.ingrid.utils.IBus;

@Service
public class CommunicationInterface {

    private IBus _nonCacheableIBus;
    private File _communicationFile;
    private ICommunication _communication;

    public CommunicationInterface() throws Exception {
        String communication = System.getProperty("communication");
        _communicationFile = new File(communication);
        if (_communicationFile.exists()) {
            restart();
        }
    }

    public IBus getIBus() {
        return _nonCacheableIBus;
    }

    public void restart() throws Exception {
        if (_communication != null) {
            _communication.shutdown();
            Thread.sleep(3000);
        }
        _communication = StartCommunication.create(new FileInputStream(_communicationFile));
        _communication.startup();
        _nonCacheableIBus = BusClientFactory.createBusClient(_communication).getNonCacheableIBus();
    }

    public String getPeerName() {
        return _communication.getPeerName();
    }

    public File getCommunicationFile() {
        return _communicationFile;
    }

}
