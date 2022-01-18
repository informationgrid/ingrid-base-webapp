/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.admin.service;

import java.io.File;
import java.util.Objects;

import de.ingrid.admin.Config;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.admin.JettyStarter;
import de.ingrid.ibus.client.BusClient;
import de.ingrid.ibus.client.BusClientFactory;
import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IPlug;

@Service
public class CommunicationService {

    protected static final Logger LOG = Logger.getLogger(CommunicationService.class);

    private final IPlug _iPlug;

    private File _communicationFile = null;

    private boolean _error = false;

    private Config config;

    // JettyStarter is needed since it depends on it for initialization
    @Autowired
    public CommunicationService(final IPlug iPlug, Config config, JettyStarter jettyStarter) {
        _iPlug = iPlug;
        this.config = config;
        boolean iBusDisabled = config.disableIBus;

        if (!iBusDisabled) {
            _communicationFile = new File(config.communicationLocation);
            if (!_communicationFile.exists()) {
                LOG.warn("communication does not exist. please create one via ui setup.");
            }
            getBusClient();
        }
    }

    public String getPeerName() {
        final BusClient busClient = getBusClient();
        return busClient == null ? "no bus" : busClient.getPeerName();
    }

    public boolean isConnected() {
        final BusClient busClient = getBusClient();
        return busClient != null && busClient.allConnected();
    }
    
    /**
     * Check if the nth connection is established.
     * @param pos is the connection number
     * @return true, if the nth connection is still connected
     */
    public boolean isConnected(int pos) {
        final BusClient busClient = getBusClient();
        return busClient != null && busClient.isConnected(pos);
    }

    private void reconfigure() {
        if (_iPlug instanceof HeartBeatPlug) {
            ((HeartBeatPlug) _iPlug).reconfigure();
        }
    }

    public void start() {
        try {
            Objects.requireNonNull(getBusClient()).start();
            _error = false;
        } catch (final Exception e) {
            LOG.warn("some of the busses are not available");
            _error = true;
        }
        reconfigure();
    }

    public void shutdown() {
        try {
            Objects.requireNonNull(getBusClient()).shutdown();
            _error = false;
        } catch (final Exception e) {
            LOG.warn("some of the busses are not available");
            _error = true;
        }
        reconfigure();
    }

    public void restart() {
        try {
            if (_iPlug instanceof HeartBeatPlug) {
                ((HeartBeatPlug) _iPlug).stopHeartBeats();
            }
            Objects.requireNonNull(getBusClient()).restart();
            _error = false;
        } catch (final Exception e) {
            LOG.warn("some of the busses are not available");
            _error = true;
        }
        reconfigure();
    }

    public IBus getIBus() {
        final BusClient busClient = getBusClient();
        return busClient == null ? null : busClient.getNonCacheableIBus();
    }

    public File getCommunicationFile() {
        return _communicationFile;
    }

    public boolean hasErrors() {
        boolean iBusDisabled = config.disableIBus;
        return !iBusDisabled && (_communicationFile.exists() && (_error || !isConnected()));
    }

    private BusClient getBusClient() {
        boolean iBusDisabled = config.disableIBus;
        if (iBusDisabled) return null;

        BusClient busClient = BusClientFactory.getBusClient();
        if (busClient == null) {
            try {
                busClient = BusClientFactory.createBusClient(_communicationFile, _iPlug);
                _error = false;
            } catch (final Exception e) {
                LOG.warn("error creating bus client");
                _error = true;
                throw new RuntimeException( "error creating bus client" );
            }
        }
        return busClient;
    }
}
