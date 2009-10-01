package de.ingrid.admin.service;

import java.io.File;
import java.io.FileInputStream;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication.messaging.IMessageHandler;
import net.weta.components.communication.messaging.IMessageQueue;
import net.weta.components.communication.reflect.ReflectMessageHandler;
import net.weta.components.communication.tcp.StartCommunication;
import net.weta.components.communication.tcp.TcpCommunication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.ibus.client.BusClientFactory;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IPlug;

@Service
public class CommunicationInterface {

    private IBus _nonCacheableIBus;
    private final File _communicationFile;
    private ICommunication _communication;
    private IPlug _plug;
    private static final Log LOG = LogFactory.getLog(CommunicationInterface.class);

    public CommunicationInterface() throws Exception {
        final String communication = System.getProperty("communication");
        _communicationFile = new File(communication);
        if (_communicationFile.exists()) {
            restart();
        }
    }

    public IBus getIBus() {
        return _nonCacheableIBus;
    }

    public void shutdown() throws Exception {
        if (_communication != null) {
            LOG.info("shutdown communication");
            _communication.closeConnection(getUrl());
        }
    }

    public void start() throws Exception {
        for (int i = 0; i < 10; i++) {
            // sleep till disconnected
            if (isConnected()) {
                Thread.sleep(500);
            } else {
                // connect
                LOG.info("create communication");
                _communication = StartCommunication.create(new FileInputStream(_communicationFile));
                LOG.info("start communication");
                _communication.startup();
                _nonCacheableIBus = BusClientFactory.createBusClient(_communication).getNonCacheableIBus();
                addPlugToCommunication(_plug);
                // sleep till connected
                while (!isConnected()) {
                    Thread.sleep(500);
                }
                break;
            }
        }
    }

    public boolean isConnected() {
        if (_communication != null) {
            return _communication.isConnected(getUrl());
        }
        return false;
    }

    public void restart() throws Exception {
        LOG.info("restart communication");

        shutdown();

        start();
    }

    private void addPlugToCommunication(final IPlug plug) {
        if (plug != null && _communication != null) {
            final IMessageQueue messageQueue = _communication.getMessageQueue();
            final IMessageHandler messageHandler = new ReflectMessageHandler();
            LOG.info("add iplug [" + plug.getClass().getSimpleName() + "] to message handler");
            ((ReflectMessageHandler) messageHandler).addObjectToCall(IPlug.class, plug);
            LOG.info("add message handler to message queue");
            messageQueue.addMessageHandler(ReflectMessageHandler.MESSAGE_TYPE, messageHandler);
        }
    }

    public String getPeerName() {
        return _communication.getPeerName();
    }

    public File getCommunicationFile() {
        return _communicationFile;
    }

    @Autowired(required = false)
    public void setPlug(final IPlug plug) {
        _plug = plug;
        addPlugToCommunication(_plug);
    }

    public final String getUrl() {
        if (_communication != null) {
            return (String) ((TcpCommunication) _communication).getServerNames().get(0);
        }
        return null;
    }
}
