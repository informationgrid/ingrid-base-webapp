package de.ingrid.admin;

import java.io.File;
import java.io.IOException;

import org.springframework.stereotype.Service;

import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.xml.XMLSerializer;

@Service
public class PlugDescriptionService {

    public PlugDescription readPlugDescription() throws IOException {
        String property = System.getProperty("plugDescription");
        File plugDescriptionFile = new File(property);
        PlugDescription plugDescription = (PlugDescription) new XMLSerializer().deSerialize(plugDescriptionFile);
        return plugDescription;

    }
}
