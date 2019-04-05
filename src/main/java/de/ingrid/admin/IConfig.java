/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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
package de.ingrid.admin;

import java.util.Properties;

import de.ingrid.admin.command.PlugdescriptionCommandObject;

public interface IConfig {
    public void initialize();
    
    /**
     * When the PlugDescription-parameters are read during startup, then we can
     * add more values coming from an iPlug. Afterwards the Plugdescription can
     * be written and used by the application.
     * 
     * @param pdObject contains an already filled PlugDescription object which can
     * be extended with more fields. This object will be used for storing. 
     */
    public void addPlugdescriptionValues(PlugdescriptionCommandObject pdObject);

    /**
     * Before writing the settings made in the plugdescription, these can be extended
     * by additional parameters from the iPlug.
     * 
     * @param props
     * @param pd
     */
    public void setPropertiesFromPlugdescription( Properties props, PlugdescriptionCommandObject pd );
    
}
