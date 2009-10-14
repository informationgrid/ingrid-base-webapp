package de.ingrid.admin.controller;

import de.ingrid.admin.IKeys;

public class AbstractController {

    public static String redirect(final String uri) {
        return IKeys.REDIRECT + uri;
    }
}
