package de.ingrid.admin.service;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.ibus.client.BusClient;
import de.ingrid.ibus.client.BusClientFactory;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IPlug;

@Service
public class CommunicationInterface {

    private final File _communicationFile;

    public CommunicationInterface() throws Exception {
        // create file
        final String communication = System.getProperty("communication");
        _communicationFile = new File(communication);
        // create bus client
        BusClientFactory.createBusClient(_communicationFile);
    }

    public String getPeerName() {
        return getBusClient().getPeerName();
    }

    public boolean isConnected() {
        return getBusClient().allConnected();
    }

    public void start() throws Exception {
        getBusClient().start();
    }

    public void shutdown() throws Exception {
        getBusClient().shutdown();
    }

    public void restart() throws Exception {
        getBusClient().restart();
    }

    public IBus getIBus() {
        return getBusClient().getNonCacheableIBus();
    }

    public BusClient getBusClient() {
        return BusClientFactory.getBusClient();
    }

    public File getCommunicationFile() {
        return _communicationFile;
    }

    @Autowired(required = false)
    public void setPlug(final IPlug plug) throws Exception {
        getBusClient().setIPlug(plug);
    }

}
