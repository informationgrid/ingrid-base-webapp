package de.ingrid.admin;

import java.io.File;
import java.io.IOException;

import org.springframework.stereotype.Service;

import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.xml.XMLSerializer;

@Service
public class PlugDescriptionService {

    public PlugDescription readPlugDescription() throws IOException {
        File file = getPlugdescriptionAsFile();
        PlugDescription plugDescription = (PlugDescription) new XMLSerializer().deSerialize(file);
        return plugDescription;
    }

    public boolean existsPlugdescription() {
        File file = getPlugdescriptionAsFile();
        return file.exists();
    }

    public File getPlugdescriptionAsFile() {
        String property = System.getProperty("plugDescription");
        File plugDescriptionFile = new File(property);
        return plugDescriptionFile;
    }
}
