package de.ingrid.admin.command;

public class FieldQueryCommandObject {

    private String _busUrl;

    private String _regex = ".*";

    private String _key;

    private String _value;

    private String _option = "required";

    public FieldQueryCommandObject() {

    }

    public void setBusUrl(final String busUrl) {
        _busUrl = busUrl;
    }

    public String getBusUrl() {
        return _busUrl;
    }

    public void setRegex(final String regex) {
        _regex = regex;
    }

    public String getRegex() {
        return _regex;
    }

    public void setKey(final String key) {
        _key = key;
    }

    public String getKey() {
        return _key;
    }

    public void setValue(final String value) {
        _value = value;
    }

    public String getValue() {
        return _value;
    }

    public void setOption(final String option) {
        _option = option;
    }

    public String getOption() {
        return _option;
    }

    public void setProhibited() {
        _option = "prohibited";
    }

    public Boolean getProhibited() {
        return "prohibited".equals(_option);
    }

    public void setRequired() {
        _option = "required";
    }

    public Boolean getRequired() {
        return "required".equals(_option);
    }
}
