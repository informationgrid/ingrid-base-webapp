package de.ingrid.admin.service;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.admin.IKeys;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.xml.XMLSerializer;

@Service
public class PlugDescriptionService {

    protected static final Logger LOG = Logger.getLogger(PlugDescriptionService.class);

    private final File _plugDescriptionFile;

    private PlugDescription _plugDescription;

    private final HeartBeatPlug _plug;

    @Autowired
    public PlugDescriptionService(final HeartBeatPlug plug) throws IOException {
        _plug = plug;
        _plugDescriptionFile = new File(System.getProperty(IKeys.PLUG_DESCRIPTION));
        if (existsPlugDescription()) {
            _plugDescription = loadPlugDescription();
            _plug.configure(_plugDescription);
            _plug.startHeartBeats();
        } else {
            LOG.warn("plug description does not exist. please create one via ui setup.");
        }
    }

    public PlugDescription getPlugDescription() throws IOException {
        if (_plugDescription == null && existsPlugDescription()) {
            _plugDescription = loadPlugDescription();
        }
        return _plugDescription;
    }

    @SuppressWarnings("unchecked")
    public void savePlugDescription(final PlugDescription plugDescription) throws Exception {
        LOG.info("saving plug description.");
        if (plugDescription instanceof PlugdescriptionCommandObject) {
            _plugDescription = new PlugDescription();
            _plugDescription.putAll(plugDescription);
        } else {
            _plugDescription = plugDescription;
        }
        final XMLSerializer serializer = new XMLSerializer();
        serializer.serialize(_plugDescription, _plugDescriptionFile);
    }

    public boolean existsPlugDescription() {
        return _plugDescriptionFile.exists();
    }

    private PlugDescription loadPlugDescription() throws IOException {
        return (PlugDescription) new XMLSerializer().deSerialize(_plugDescriptionFile);
    }
}
