/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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
package de.ingrid.admin.object;


/**
 * This datatype can be configured and injected via spring !
 * E.g. add arbitrary datatypes for "tagging" of iPlug to be used for facetting !
 */
public class UserDefinedDataType extends AbstractDataType {

    public UserDefinedDataType(String dataTypeName) {
        super(dataTypeName);
    }

    public UserDefinedDataType(String dataTypeName, Boolean isHidden) {
        super(dataTypeName, isHidden);
    }

    /* (non-Javadoc)
     * @see de.ingrid.admin.object.AbstractDataType#setForceActive(boolean)
     */
    public void setForceActive(boolean force) {
        super.setForceActive(force);
    }

    /**
     * @return datatype always set (true) or has to be clicked by user in admin gui (false)
     */
    public boolean getForceActive() {
        return super.getIsForced();
    }
}
