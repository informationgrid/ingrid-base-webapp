package de.ingrid.admin;

import org.springframework.stereotype.Service;

import de.ingrid.ibus.client.BusClientFactory;
import de.ingrid.utils.IBus;

@Service
public class CommunicationInterface {

    private IBus _nonCacheableIBus;

    public CommunicationInterface() throws Exception {
        _nonCacheableIBus = BusClientFactory.createBusClient().getNonCacheableIBus();
    }

    public IBus getIBus() {
        return _nonCacheableIBus;
    }

}
