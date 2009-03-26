package de.ingrid.admin;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.ingrid.utils.PlugDescription;

@Service
@Qualifier("flipIndex")
public class FlipIndexListener implements INewIndexListener {

    private final INewIndexListener _listener;
    private final PlugDescriptionService _plugDescriptionService;

    @Autowired
    public FlipIndexListener(@Qualifier("restartSearcher") INewIndexListener listener,
            PlugDescriptionService plugDescriptionService) {
        _listener = listener;
        _plugDescriptionService = plugDescriptionService;
    }

    public void indexIsCreated() throws IOException {
        PlugDescription plugDescription = _plugDescriptionService.readPlugDescription();
        File workinDirectory = plugDescription.getWorkinDirectory();
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

}
