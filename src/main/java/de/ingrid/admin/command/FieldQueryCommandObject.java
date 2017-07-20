/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
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
