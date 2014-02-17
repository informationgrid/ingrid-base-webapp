package de.ingrid.admin;

import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertiesFiles;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertyLocations;
import com.tngtech.configbuilder.annotation.valueextractor.DefaultValue;
import com.tngtech.configbuilder.annotation.valueextractor.PropertyValue;
import com.tngtech.configbuilder.annotation.valueextractor.SystemPropertyValue;

@PropertiesFiles( {"config", "database"} )
@PropertyLocations(directories = {"conf"}, fromClassLoader = true)
public class Config {

	@SystemPropertyValue("jetty.webapp")
	@PropertyValue("jetty.webapp")
	@DefaultValue("webapp")
	private String webappDir;
	
	@SystemPropertyValue("jetty.port")
	@PropertyValue("jetty.port")
	@DefaultValue("8082")
	private Integer webappPort;
	
	public String getWebappDir() { return this.webappDir; }
	public Integer getWebappPort() { return this.webappPort; }
}
