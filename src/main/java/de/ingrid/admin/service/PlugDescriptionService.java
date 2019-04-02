/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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
import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import de.ingrid.admin.IKeys;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.xml.PlugdescriptionSerializer;

@Service
public class PlugDescriptionService {

	protected static final Logger LOG = Logger.getLogger(PlugDescriptionService.class);

	private final File _plugDescriptionFile;

	private PlugDescription _plugDescription;

    public PlugDescriptionService() throws IOException {
		_plugDescriptionFile = new File(System.getProperty(IKeys.PLUG_DESCRIPTION));
		if (existsPlugDescription()) {
			_plugDescription = loadPlugDescription();
		} else {
			LOG.warn("plug description does not exist (mind case sensitive file names). please create one via ui setup.");
		}
	}

	public PlugDescription getPlugDescription() throws IOException {
	    if (_plugDescription == null && existsPlugDescription()) {
            _plugDescription = loadPlugDescription();
		}
		return _plugDescription;
	}

    public PlugdescriptionCommandObject getCommandObect() throws IOException {
        return new PlugdescriptionCommandObject(_plugDescriptionFile);
    }

	public void savePlugDescription(final PlugDescription plugDescription)
			throws Exception {
		LOG.info("saving plug description.");
		PlugDescription tmpDesc = plugDescription;
		if (plugDescription instanceof PlugdescriptionCommandObject) {
            tmpDesc = new PlugDescription();
            //tmpDesc.putAll(plugDescription);
            // only add non-null values!
            Iterator<?> keyIt = plugDescription.keySet().iterator();
            while (keyIt.hasNext()) {
                Object next = keyIt.next();
                if (plugDescription.get(next) != null ) {
                    tmpDesc.put( next, plugDescription.get(next) );
                }
            }
		}
		final PlugdescriptionSerializer serializer = new PlugdescriptionSerializer();
		serializer.serialize(tmpDesc, _plugDescriptionFile);
		// load again to set the serialized folder
		_plugDescription = loadPlugDescription();
	}

	public boolean existsPlugDescription() {
		return _plugDescriptionFile.exists();
	}

	public boolean isIPlugSecured() {
	    return existsPlugDescription() && _plugDescription.getIplugAdminPassword() != null;
	}

    public PlugDescription reloadPlugDescription() throws IOException {
        _plugDescription = null;
        return getPlugDescription();
    }

	private PlugDescription loadPlugDescription() throws IOException {
		LOG.info("load plugdescription from file: "
				+ _plugDescriptionFile.getAbsolutePath());
		return new PlugdescriptionSerializer()
				.deSerialize(_plugDescriptionFile);
	}
}
