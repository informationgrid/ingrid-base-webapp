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
import java.util.regex.Pattern;

import net.weta.components.communication.configuration.XPathService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;

import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertiesFiles;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertyLocations;
import com.tngtech.configbuilder.annotation.typetransformer.CharacterSeparatedStringToStringListTransformer;
import com.tngtech.configbuilder.annotation.typetransformer.TypeTransformer;
import com.tngtech.configbuilder.annotation.typetransformer.TypeTransformers;
import com.tngtech.configbuilder.annotation.valueextractor.DefaultValue;
import com.tngtech.configbuilder.annotation.valueextractor.PropertyValue;
import com.tngtech.configbuilder.annotation.valueextractor.SystemPropertyValue;

import de.ingrid.admin.command.CommunicationCommandObject;
import de.ingrid.admin.command.FieldQueryCommandObject;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.controller.CommunicationConfigurationController;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.QueryExtension;
import de.ingrid.utils.QueryExtensionContainer;
import de.ingrid.utils.query.FieldQuery;

@PropertiesFiles( {"config", "database"} )
@PropertyLocations(directories = {"conf"}, fromClassLoader = true)
public class Config {
    
    private static Log log = LogFactory.getLog(Config.class);
    
    private static String QUERYTYPE_ALLOW  = "allow";
    private static String QUERYTYPE_DENY   = "deny";
    public static String QUERYTYPE_MODIFY = "modify";

    
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
    
    public class StringToQueryExtension extends TypeTransformer<String, List<FieldQueryCommandObject>>{

        @Override
        public List<FieldQueryCommandObject> transform( String input ) {
            List<FieldQueryCommandObject> list = new ArrayList<FieldQueryCommandObject>();
            String[] split = input.split( "##" );
            for (String extensions : split) {
                String[] extArray = extensions.split( "," );
                if (extArray.length == 7) {
                    FieldQueryCommandObject commObject = new FieldQueryCommandObject();
                    commObject.setBusUrl( extArray[0] );
                    commObject.setRegex( extArray[1] );
                    commObject.setOption( extArray[2] );
                    commObject.setKey( extArray[3] );
                    commObject.setValue( extArray[4] );
                    if ("true".equals( extArray[5] )) {
                        commObject.setRequired();
                    }
                    if ("true".equals( extArray[6] )) {
                        commObject.setProhibited();
                    }
                    list.add( commObject );
                } else {
                    log.error( "QueryExtension could not be extracted, because of missing values. Expected 7 but got: " + extArray.length );
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
	
	@TypeTransformers(CharacterSeparatedStringToStringListTransformer.class)
	@PropertyValue("plugdescription.partner")
	private List<String> partner;
	
	@TypeTransformers(CharacterSeparatedStringToStringListTransformer.class)
	@PropertyValue("plugdescription.provider")
	private List<String> provider;
	
	@TypeTransformers(StringToQueryExtension.class)
	@PropertyValue("plugdescription.queryExtensions")
	private List<FieldQueryCommandObject> queryExtensions;	
	
	public String getWebappDir() { return this.webappDir; }
	public Integer getWebappPort() { return this.webappPort; }
	public String getCommunicationLocation() { return this.communicationLocation; }
	public String getPlugdescription() { return this.plugdescriptionLocation; }
	
	public void initialize() {
	    ClassPathResource confOverride = new ClassPathResource( "config.override.properties" );
        // create override file if it does not exist
	    try {
	        File file = confOverride.getFile();
            if ( !file.exists() ) {
                FileOutputStream fileOutputStream;
                fileOutputStream = new FileOutputStream( file );
                fileOutputStream.close();
            }
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}
	    
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
	
	/*public boolean writeConfig(String key, String value) {
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
	}*/
	
	public boolean writeCommunication() {
	    File communicationFile = new File( this.communicationLocation );
	    if (ibusses == null || ibusses.isEmpty()) {
	        if (communicationFile.exists()) {
	            communicationFile.delete();
	        }
	        return true;
	    }
	    
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
            ClassPathResource override = new ClassPathResource( "config.override.properties" );
            InputStream is = new FileInputStream( override.getFile().getAbsolutePath() );
            Properties props = new Properties();
            props.load( is );
            // ---------------------------
            props.setProperty( "communication.clientName", communicationProxyUrl );            
            
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
            OutputStream os = new FileOutputStream( override.getFile().getAbsolutePath() ); 
            props.store( os, "Override configuration written by the application" );
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public void setQueryExtensions(List<FieldQueryCommandObject> fieldQueries) {
	    this.queryExtensions = fieldQueries;
	}
	
	public void writePlugdescriptionToProperties(PlugdescriptionCommandObject pd) {
	    try {
	        // TODO: write all properties to class variables first, to synchronize values!
	        // ...
	        
	        ClassPathResource override = new ClassPathResource( "config.override.properties" );
    	    InputStream is = new FileInputStream( override.getFile().getAbsolutePath() );
            Properties props = new Properties();
            props.load( is );
            // ---------------------------
            props.setProperty( "plugdescription.workingdir", pd.getWorkinDirectory().getPath() );
            props.setProperty( "plugdescription.datatypes", convertArrayToString( pd.getDataTypes() ));
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
            
            props.setProperty( "plugdescription.partner", convertArrayToString( pd.getPartners() ));
            props.setProperty( "plugdescription.provider", convertArrayToString( pd.getProviders() ));
            
            props.setProperty( "plugdescription.queryExtensions", convertQueryExtensionsToString( this.queryExtensions ));
            
            IConfig externalConfig = JettyStarter.getInstance().getExternalConfig();
            if (externalConfig != null) {
                externalConfig.setPropertiesFromPlugdescription(props, pd);
                externalConfig.addPlugdescriptionValues( pd );
            }
            
            
            // ---------------------------
            is.close();
            OutputStream os = new FileOutputStream( override.getFile().getAbsolutePath() ); 
            props.store( os, "Override configuration written by the application" );
            os.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public void addQueryExtensionsToProperties(FieldQueryCommandObject fq) {
	    if (this.queryExtensions == null) this.queryExtensions = new ArrayList<FieldQueryCommandObject>();
	    this.queryExtensions.add( fq );
	}
	
	public void removeQueryExtensionsFromProperties(FieldQueryCommandObject fq) {
	    for (FieldQueryCommandObject ext : this.queryExtensions) {
            if (ext.getBusUrl().equals( fq.getBusUrl() ) && ext.getRegex().equals( fq.getRegex() )
                    && ext.getKey().equals( fq.getKey() ) && ext.getValue().equals( fq.getValue() )
                    && ext.getProhibited().equals( fq.getProhibited() ) && ext.getRequired().equals( fq.getRequired() )) {
                
                this.queryExtensions.remove( ext );
                break;
            }
        }
	}
	
	private String convertQueryExtensionsToString ( List<FieldQueryCommandObject> queryExtensions ) {
	    String result = "";
	    if (queryExtensions != null) {
            for (FieldQueryCommandObject fq : queryExtensions) {
                result += fq.getBusUrl() + ",";
                result += fq.getRegex() + ",";
                result += fq.getOption() + ",";
                result += fq.getKey() + ",";
                result += fq.getValue() + ",";
                result += fq.getRequired() + ",";
                result += fq.getProhibited() + "##";
            }
            if (!result.isEmpty()) {
                return result.substring( 0, result.length() - 2 );
            }
	    }
        return result;
    }
	
    private String convertArrayToString(String[] list) {
	    String dataTypesAsString = Arrays.toString( list );
	    return dataTypesAsString.substring( 1,  dataTypesAsString.length() - 1 );
	}
	
	public PlugdescriptionCommandObject getPlugdescriptionFromProperties() {
	    PlugdescriptionCommandObject pd = new PlugdescriptionCommandObject();
	    
	    // working directory
	    File pdDir = new File(this.pdWorkingDir);
	    pdDir.mkdirs();
	    pd.setWorkinDirectory( pdDir );
	    pd.setProxyServiceURL( this.communicationProxyUrl );
	    
	    pd.remove( PlugDescription.DATA_TYPE );
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
        
        if (partner != null) {
            for (String p : partner) {
                pd.addPartner( p.trim() );
            }
        }
        
        if (provider != null) {
            for (String p : provider) {
                pd.addProvider( p.trim() );
            }
        }
        
        if (this.queryExtensions != null) {
	        for (FieldQueryCommandObject fq : this.queryExtensions) {
	            addFieldQuery( pd, fq, QUERYTYPE_MODIFY);            
	        }
        }
        
	    return pd;
	    
	}
	
	public static void addFieldQuery(final PlugdescriptionCommandObject commandObject,
            final FieldQueryCommandObject fieldQuery, String behaviour) {
        // get container
        QueryExtensionContainer container = commandObject.getQueryExtensionContainer();
        if (null == container) {
            // create container
            container = new QueryExtensionContainer();
            commandObject.setQueryExtensionContainer(container);
        }
        // get extension
        QueryExtension extension = container.getQueryExtension(fieldQuery.getBusUrl());
        if (null == extension) {
            // create extension
            extension = new QueryExtension();
            extension.setBusUrl(fieldQuery.getBusUrl());
            container.addQueryExtension(extension);
        }
        // create field query
        final Pattern pattern = Pattern.compile(fieldQuery.getRegex());
        FieldQuery fq = null;
        if (behaviour.equals(QUERYTYPE_MODIFY)) {
            fq = new FieldQuery(fieldQuery.getRequired(), fieldQuery.getProhibited(), fieldQuery.getKey(),
                fieldQuery.getValue());
        } else if (behaviour.equals(QUERYTYPE_DENY)) {
            fq = new FieldQuery(fieldQuery.getRequired(), fieldQuery.getProhibited(), "metainfo", "query_deny");
            fieldQuery.setKey( "metainfo" );
            fieldQuery.setValue( "query_deny" );
        } else if (behaviour.equals(QUERYTYPE_ALLOW)) {
            fq = new FieldQuery(fieldQuery.getRequired(), fieldQuery.getProhibited(), "metainfo", "query_allow");
            fieldQuery.setKey( "metainfo" );
            fieldQuery.setValue( "query_allow" );
        }
        extension.addFieldQuery(pattern, fq);
    }

}