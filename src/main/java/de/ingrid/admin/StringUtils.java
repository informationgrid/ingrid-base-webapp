package de.ingrid.admin;


public class StringUtils {

    public static boolean isEmpty(final String s) {
        if (null == s || s.length() <= 0) {
            return true;
        }
        return false;
    }

    public static boolean isEmptyOrWhiteSpace(final String s) {
        if (!isEmpty(s)) {
            if (s.trim().length() > 0) {
                return false;
            }
        }
        return true;
    }
}
