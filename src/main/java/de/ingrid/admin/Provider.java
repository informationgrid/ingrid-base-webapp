package de.ingrid.admin;

public class Provider {

    private final String _shortName;
    private final String _displayName;

    public Provider(String shortName, String displayName) {
        _shortName = shortName;
        _displayName = displayName;
    }

    public String getShortName() {
        return _shortName;
    }

    public String getDisplayName() {
        return _displayName;
    }
}
