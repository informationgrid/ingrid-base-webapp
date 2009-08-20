package de.ingrid.admin;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.PlugDescription;

@Service
@Qualifier("flipIndex")
public class FlipIndexListener implements INewIndexListener, IConfigurable {

    private final INewIndexListener _listener;
    private PlugDescription _plugDescription;
    private static final Log LOG = LogFactory.getLog(FlipIndexListener.class);

    @Autowired
    public FlipIndexListener(@Qualifier("restartSearcher") INewIndexListener listener) {
        _listener = listener;
    }

    public void indexIsCreated() throws IOException {
        File workinDirectory = _plugDescription.getWorkinDirectory();
        File oldIndex = new File(workinDirectory, "index");
        File newIndex = new File(workinDirectory, "newIndex");
        delete(oldIndex);
        newIndex.renameTo(oldIndex);
        _listener.indexIsCreated();
    }

    private void delete(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isDirectory()) {
                    delete(file);
                }
                file.delete();
            }
        }
        folder.delete();
    }

    public void configure(PlugDescription plugDescription) {
        LOG.debug("reconfigure...");
        _plugDescription = plugDescription;
    }

}
