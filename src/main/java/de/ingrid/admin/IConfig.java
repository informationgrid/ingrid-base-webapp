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
