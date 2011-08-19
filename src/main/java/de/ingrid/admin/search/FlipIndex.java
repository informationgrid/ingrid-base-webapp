package de.ingrid.admin.search;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.PlugDescription;

public abstract class FlipIndex implements IConfigurable {

    private static final Log LOG = LogFactory.getLog(FlipIndex.class);

    public void configure(PlugDescription plugDescription) {
        LOG.info("configure index directory...");
        File workinDirectory = plugDescription.getWorkinDirectory();
        File oldIndex = new File(workinDirectory, "index");
        File newIndex = new File(workinDirectory, "newIndex");
        if (newIndex.exists()) {
            LOG.info("delete index: " + oldIndex);
            delete(oldIndex);
            LOG.info("rename index: " + newIndex);
            if (!newIndex.renameTo(oldIndex)) {
                LOG.warn("Unable to rename '" + newIndex.getAbsolutePath() + "' to '" + oldIndex.getAbsolutePath() + "'");
            }
        }
    }

    private void delete(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isDirectory()) {
                    delete(file);
                }
                if (!file.delete()) {
                    LOG.warn("Unable to delete file: " + file.getAbsolutePath());
                }
            }
        }
        if (!folder.delete()) {
            LOG.warn("Unable to delete folder: " + folder.getAbsolutePath());
        }
    }

}
