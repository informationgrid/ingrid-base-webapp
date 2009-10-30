package de.ingrid.admin;

import java.io.File;

public class TestUtils {

    public static void delete(final File folder) {
        final File[] files = folder.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.isDirectory()) {
                    delete(file);
                } else {
                    file.delete();
                }
            }
        }
        folder.delete();
    }
}
