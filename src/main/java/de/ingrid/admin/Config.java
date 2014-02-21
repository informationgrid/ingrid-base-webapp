package de.ingrid.admin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import net.weta.components.communication.configuration.XPathService;

import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertiesFiles;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertyLocations;
import com.tngtech.configbuilder.annotation.typetransformer.CharacterSeparatedStringToStringListTransformer;
import com.tngtech.configbuilder.annotation.typetransformer.TypeTransformer;
import com.tngtech.configbuilder.annotation.typetransformer.TypeTransformers;
import com.tngtech.configbuilder.annotation.valueextractor.DefaultValue;
import com.tngtech.configbuilder.annotation.valueextractor.PropertyValue;
import com.tngtech.configbuilder.annotation.valueextractor.SystemPropertyValue;

import de.ingrid.admin.command.CommunicationCommandObject;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.controller.CommunicationConfigurationController;

@PropertiesFiles( {"config", "database"} )
@PropertyLocations(directories = {"conf"}, fromClassLoader = true)
public class Config {
    
    public class StringToCommunications extends TypeTransformer<String, List<CommunicationCommandObject>>{

        @Override
        public List<CommunicationCommandObject> transform( String input ) {
            List<CommunicationCommandObject> list = new ArrayList<CommunicationCommandObject>();
            String[] split = input.split( "##" );
            for (String comm : split) {
                String[] communication = comm.split( "," );
                if (communication.length == 3) {
                    CommunicationCommandObject commObject = new CommunicationCommandObject();
                    commObject.setBusProxyServiceUrl( communication[0] );
                    commObject.setIp( communication[1] );
                    commObject.setPort( Integer.valueOf( communication[2] ) );
                    list.add( commObject );
                }
            }
            return list;
        }
        
    }
    
    public static final int DEFAULT_TIMEOUT = 10;

    public static final int DEFAULT_MAXIMUM_SIZE = 1048576;

    public static final int DEFAULT_THREAD_COUNT = 100;

    /**
     * SERVER - CONFIGURATION
     */
	@SystemPropertyValue("jetty.webapp")
	@PropertyValue("jetty.webapp")
	@DefaultValue("webapp")
	public String webappDir;
	
	@SystemPropertyValue("jetty.port")
	@PropertyValue("jetty.port")
	@DefaultValue("8082")
	public Integer webappPort;
	
	
	/**
	 * COMMUNICATION - SETTINGS
	 */
	@SystemPropertyValue("communication")
	@PropertyValue("communication.location")
	@DefaultValue("conf/communication.xml")
	public String communicationLocation;
	
	@PropertyValue("communication.clientName")
	@DefaultValue("/ingrid-group:base-webapp")
	public String communicationProxyUrl;
	
	@TypeTransformers(Config.StringToCommunications.class)
	@PropertyValue("communications.ibus")
	@DefaultValue("")
	public List<CommunicationCommandObject> ibusses;
	
	
	/**
	 * PLUGDESCRIPTION
	 */
	@SystemPropertyValue("plugdescription")
	@DefaultValue("conf/plugdescription.xml")
	private String plugdescriptionLocation;
	
	@PropertyValue("plugdescription.workingdir")
	@DefaultValue(".")
	public String pdWorkingDir;
	
	@PropertyValue("plugdescription.password")
    public String pdPassword;
	
	
	
	@SystemPropertyValue("indexing")
	@PropertyValue("indexing")
	@DefaultValue("false")
	private String indexing;

	@TypeTransformers(CharacterSeparatedStringToStringListTransformer.class)
	@PropertyValue("plugdescription.datatypes")
	private List<String> datatypes;
	
	@PropertyValue("plugdescription.person.title")
    private String personTitle;

	@PropertyValue("plugdescription.person.name")
    private String personName;

	@PropertyValue("plugdescription.person.surname")
    private String personSurname;

	@PropertyValue("plugdescription.person.email")
    private String personEmail;

	@PropertyValue("plugdescription.person.phone")
    private String personPhone;

	@PropertyValue("plugdescription.datasource.name")
    private String datasourceName;

	@PropertyValue("plugdescription.datasource.description")
    private String datasourceDescription;

	@PropertyValue("plugdescription.guiUrl")
    private String guiUrl;
	
	public String getWebappDir() { return this.webappDir; }
	public Integer getWebappPort() { return this.webappPort; }
	public String getCommunicationLocation() { return this.communicationLocation; }
	public String getPlugdescription() { return this.plugdescriptionLocation; }
	
	public void initialize() {
	    // set system property for use in JSP file!
	    if ("true".equals( indexing )) {
	        System.setProperty( IKeys.INDEXING, "true" );
	    }
	    
	    // plug description
        String plugDescription = System.getProperty(IKeys.PLUG_DESCRIPTION);
        if (plugDescription == null) {
            System.setProperty(IKeys.PLUG_DESCRIPTION, plugdescriptionLocation);
        }
        
        // 
	    writeCommunication();
	}
	public String getIndexing() { return this.indexing; }
	
	public boolean writeConfig(String key, String value) {
        try {
            InputStream is = new FileInputStream( "conf/config.override.properties" );
            Properties props = new Properties();
            props.load( is );
            props.setProperty( key, value );
            is.close();
            OutputStream os = new FileOutputStream( "conf/config.override.properties" ); 
            props.store( os, "Override configuration written by the application" );
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
	    return true;
	}
	
	public boolean writeCommunication() {
	    File communicationFile = new File( this.communicationLocation );
	    try {
            final XPathService communication = openCommunication( communicationFile );
            Integer id = 0;
            // if server information shall be deleted
//            if (serverName == null) {
//            } else {
                // check if xpath to server exists
                //boolean serverExists = communication.exsistsNode( "/communication/client/connections/server" );
            
            communication.setAttribute("/communication/client", "name", this.communicationProxyUrl);

            communication.removeNode("/communication/client/connections/server", id);
            // create default nodes and attributes if server tag does not exist
            //if (!serverExists) {
            if (ibusses != null) {
                for (CommunicationCommandObject ibus : ibusses) {
                    
                    communication.addNode("/communication/client/connections", "server");
                    communication.addNode("/communication/client/connections/server", "socket", id);
                    communication.addAttribute("/communication/client/connections/server/socket", "timeout", "" + DEFAULT_TIMEOUT, id);
                    communication.addNode("/communication/client/connections/server", "messages", id);
                    communication.addAttribute("/communication/client/connections/server/messages", "maximumSize", "" + DEFAULT_MAXIMUM_SIZE, id);
                    communication.addAttribute("/communication/client/connections/server/messages", "threadCount", "" + DEFAULT_THREAD_COUNT, id);
                    
                    communication.addAttribute("/communication/client/connections/server", "name", ibus.getBusProxyServiceUrl(), id);
                    communication.addAttribute("/communication/client/connections/server/socket", "port", "" + ibus.getPort(), id);
                    communication.addAttribute("/communication/client/connections/server/socket", "ip", ibus.getIp(), id);
                    id++;
                }
            }
                    
                /*} else {
                    communication.setAttribute("/communication/client", "name", client, id);
                    communication.setAttribute("/communication/client/connections/server", "name", serverName, id);
                    communication.setAttribute("/communication/client/connections/server/socket", "port", port, id);
                    communication.setAttribute("/communication/client/connections/server/socket", "ip", ip, id);
                }*/
            //}
            communication.store( communicationFile );
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
	    
	    return true;
	}
	
	private final XPathService openCommunication(final File communicationFile) throws Exception {
        // first of all create directories if necessary
        if (communicationFile.getParentFile() != null) {
            if (!communicationFile.getParentFile().exists()) {
                communicationFile.getParentFile().mkdirs();
            }
        }

        // open template xml or communication file
        final XPathService communication = new XPathService();
        //if (!communicationFile.exists()) {
            final InputStream inputStream = CommunicationConfigurationController.class
                    .getResourceAsStream("/communication-template.xml");
            communication.registerDocument(inputStream);
        //} else {
        //    communication.registerDocument(communicationFile);
        //}

        return communication;
    }
	
	public void writeCommunicationToProperties() {
        try {
            InputStream is = new FileInputStream( "conf/config.override.properties" );
            Properties props = new Properties();
            props.load( is );
            // ---------------------------
            String communications = "";
            for (int i = 0; i < ibusses.size(); i++) {
                CommunicationCommandObject ibus = ibusses.get( i );
                communications += ibus.getBusProxyServiceUrl() + ","
                        + ibus.getIp() + ","
                        + ibus.getPort();
                if (i != (ibusses.size()-1))
                    communications += "##";
            }
            props.setProperty( "communications.ibus", communications );
            
            // ---------------------------
            is.close();
            OutputStream os = new FileOutputStream( "conf/config.override.properties" ); 
            props.store( os, "Override configuration written by the application" );
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public void writePlugdescriptionToProperties(PlugdescriptionCommandObject pd) {
	    try {
    	    InputStream is = new FileInputStream( "conf/config.override.properties" );
            Properties props = new Properties();
            props.load( is );
            // ---------------------------
            props.setProperty( "plugdescription.workingdir", pd.getWorkinDirectory().getPath() );
            String dataTypesAsString = Arrays.toString( pd.getDataTypes() );
            props.setProperty( "plugdescription.datatypes", dataTypesAsString.substring( 1,  dataTypesAsString.length() - 1 ) );
            props.setProperty( "plugdescription.proxyServiceUrl", pd.getProxyServiceURL() );
            props.setProperty( "plugdescription.originalPort", String.valueOf( pd.getOriginalPort() ) );
            props.setProperty( "plugdescription.person.title", pd.getPersonTitle() );
            props.setProperty( "plugdescription.person.name", pd.getPersonName() );
            props.setProperty( "plugdescription.person.surname", pd.getPersonSureName() );
            props.setProperty( "plugdescription.person.email", pd.getPersonMail() );
            props.setProperty( "plugdescription.person.phone", pd.getPersonPhone() );
            props.setProperty( "plugdescription.datasource.name", pd.getDataSourceName() );
            props.setProperty( "plugdescription.datasource.description", pd.getDataSourceDescription() );
            props.setProperty( "plugdescription.guiUrl", pd.getIplugAdminGuiUrl() );
            
            props.setProperty( "plugdescription.password", pd.getIplugAdminPassword() );
            
            if (JettyStarter.getInstance().getExternalConfig() != null) {
                JettyStarter.getInstance().getExternalConfig().setPropertiesFromPlugdescription(props, pd);
            }
            
            
            // ---------------------------
            is.close();
            OutputStream os = new FileOutputStream( "conf/config.override.properties" ); 
            props.store( os, "Override configuration written by the application" );
            os.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public PlugdescriptionCommandObject getPlugdescriptionFromProperties() {
	    PlugdescriptionCommandObject pd = new PlugdescriptionCommandObject();
	    
	    // working directory
	    File pdDir = new File(this.pdWorkingDir);
	    pdDir.mkdirs();
	    pd.setWorkinDirectory( pdDir );
	    pd.setProxyServiceURL( this.communicationProxyUrl );
	    
	    if (datatypes != null) {
    	    for (String datatype : datatypes) {
    	        pd.addDataType( datatype.trim() );            
            }
	    }
	    
	    pd.setIplugAdminPassword( pdPassword );
	    
        pd.setPersonTitle( personTitle );
        pd.setPersonName( personName );
        pd.setPersonSureName( personSurname );
        pd.setPersonMail( personEmail );
        pd.setPersonPhone( personPhone );
        pd.setDataSourceName( datasourceName );
        pd.setDataSourceDescription( datasourceDescription );
        pd.setIplugAdminGuiUrl( guiUrl );
        pd.setIplugAdminGuiPort( this.webappPort );
	    
	    return pd;
	    
	}
}
