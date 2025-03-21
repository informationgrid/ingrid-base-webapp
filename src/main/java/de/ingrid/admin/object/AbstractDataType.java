/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.admin.object;

abstract public class AbstractDataType implements IDataType {

    private final String _name;

    private boolean _hidden = false;
    
    private boolean _force = false;

    private final IDataType[] _includes;

    public AbstractDataType(final String name, final IDataType... includes) {
        _name = name;
        _includes = includes;
    }

    public AbstractDataType(final String name, final boolean isHidden, final IDataType... includes) {
        _name = name;
        _hidden = isHidden;
        _includes = includes;
    }

    public String getName() {
        return _name;
    }

    public boolean isHidden() {
        return _hidden;
    }

    public IDataType[] getIncludedDataTypes() {
        return _includes;
    }

    
    public void setForceActive(boolean force) {
        this._force = force;
    }

    public boolean getIsForced() {
        return _force;
    }
}
