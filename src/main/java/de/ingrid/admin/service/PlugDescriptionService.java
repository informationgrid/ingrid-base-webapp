package de.ingrid.admin.service;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.admin.object.IPlugdescriptionFieldFilter;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.xml.XMLSerializer;

@Service
public class PlugDescriptionService {

    private IPlugdescriptionFieldFilter[] _fieldFilters = new IPlugdescriptionFieldFilter[] {};

    @Autowired(required = false)
    public void setFilters(IPlugdescriptionFieldFilter... fieldFilters) {
        _fieldFilters = fieldFilters;
    }

    public PlugDescription readPlugDescription() throws IOException {
        File file = getPlugDescriptionAsFile();
        PlugDescription plugDescription = (PlugDescription) new XMLSerializer().deSerialize(file);
        return plugDescription;
    }

    public PlugDescription readHeartBeatPlugDescription() throws IOException {
        PlugDescription plugDescription = readPlugDescription();
        Iterator iterator = plugDescription.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry entry = (Entry) iterator.next();
            Object key = entry.getKey();
            for (IPlugdescriptionFieldFilter filter : _fieldFilters) {
                if (filter.filter(key)) {
                    iterator.remove();
                    continue;
                }
            }
        }
        return plugDescription;
    }

    public boolean existsPlugdescription() {
        File file = getPlugDescriptionAsFile();
        return file.exists();
    }

    public File getPlugDescriptionAsFile() {
        String property = System.getProperty("plugDescription");
        File plugDescriptionFile = new File(property);
        return plugDescriptionFile;
    }

}
