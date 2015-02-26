/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import net.weta.components.communication.configuration.XPathService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.google.common.base.Joiner;
import com.tngtech.configbuilder.annotation.configuration.LoadingOrder;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertiesFiles;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertyLocations;
import com.tngtech.configbuilder.annotation.typetransformer.CharacterSeparatedStringToStringListTransformer;
import com.tngtech.configbuilder.annotation.typetransformer.TypeTransformer;
import com.tngtech.configbuilder.annotation.typetransformer.TypeTransformers;
import com.tngtech.configbuilder.annotation.valueextractor.DefaultValue;
import com.tngtech.configbuilder.annotation.valueextractor.EnvironmentVariableValue;
import com.tngtech.configbuilder.annotation.valueextractor.PropertyValue;
import com.tngtech.configbuilder.annotation.valueextractor.CommandLineValue;
import com.tngtech.configbuilder.annotation.valueextractor.SystemPropertyValue;

import de.ingrid.admin.command.CommunicationCommandObject;
import de.ingrid.admin.command.FieldQueryCommandObject;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.controller.CommunicationConfigurationController;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.QueryExtension;
import de.ingrid.utils.QueryExtensionContainer;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.tool.PlugDescriptionUtil;
import de.ingrid.utils.tool.QueryUtil;

@PropertiesFiles({ "config" })
@PropertyLocations(directories = { "conf" }, fromClassLoader = true)
@LoadingOrder({CommandLineValue.class, SystemPropertyValue.class, PropertyValue.class, EnvironmentVariableValue.class, DefaultValue.class})
public class Config {

    private static Log log = LogFactory.getLog( Config.class );

    private static String QUERYTYPE_ALLOW = "allow";
    private static String QUERYTYPE_DENY = "deny";
    public static String QUERYTYPE_MODIFY = "modify";

    public class StringToCommunications extends TypeTransformer<String, List<CommunicationCommandObject>> {

        @Override
        public List<CommunicationCommandObject> transform(String input) {
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

    public class StringToQueryExtension extends TypeTransformer<String, List<FieldQueryCommandObject>> {

        @Override
        public List<FieldQueryCommandObject> transform(String input) {
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
                    log.error( "QueryExtension could not be extracted, because of missing values. Expected 7 but got: "
                            + extArray.length );
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

    @PropertyValue("plugdescription.workingDirectory")
    @DefaultValue(".")
    public String pdWorkingDir;

    @PropertyValue("plugdescription.IPLUG_ADMIN_PASSWORD")
    public String pdPassword;

    @SystemPropertyValue("indexing")
    @PropertyValue("indexing")
    @DefaultValue("false")
    private String indexing;

    @TypeTransformers(CharacterSeparatedStringToStringListTransformer.class)
    @PropertyValue("plugdescription.dataType")
    private List<String> datatypes;

    @PropertyValue("plugdescription.organisationPartnerAbbr")
    private String mainPartner;

    @PropertyValue("plugdescription.organisationAbbr")
    private String mainProvider;

    @PropertyValue("plugdescription.organisation")
    private String organisation;

    @PropertyValue("plugdescription.personTitle")
    private String personTitle;

    @PropertyValue("plugdescription.personName")
    private String personName;

    @PropertyValue("plugdescription.personSureName")
    private String personSurname;

    @PropertyValue("plugdescription.personMail")
    private String personEmail;

    @PropertyValue("plugdescription.personPhone")
    private String personPhone;

    @PropertyValue("plugdescription.dataSourceName")
    private String datasourceName;

    @PropertyValue("plugdescription.dataSourceDescription")
    private String datasourceDescription;

    @PropertyValue("plugdescription.IPLUG_ADMIN_GUI_URL")
    private String guiUrl;

    @TypeTransformers(CharacterSeparatedStringToStringListTransformer.class)
    @PropertyValue("plugdescription.fields")
    @DefaultValue("")
    private List<String> fields;

    @TypeTransformers(CharacterSeparatedStringToStringListTransformer.class)
    @PropertyValue("plugdescription.partner")
    private List<String> partner;

    @TypeTransformers(CharacterSeparatedStringToStringListTransformer.class)
    @PropertyValue("plugdescription.provider")
    private List<String> provider;

    @TypeTransformers(StringToQueryExtension.class)
    @PropertyValue("plugdescription.queryExtensions")
    private List<FieldQueryCommandObject> queryExtensions;
    
    @PropertyValue("plugdescription.isRecordLoader")
    @DefaultValue("true")
    public boolean recordLoader;
    
    @PropertyValue("plugdescription.forceAddRankingOff")
    @DefaultValue("false")
    public boolean forceAddRankingOff;
    
    @PropertyValue("plugdescription.ranking")
    @DefaultValue("off")
    public List<String> rankings;

    public String getWebappDir() {
        return this.webappDir;
    }

    public Integer getWebappPort() {
        return this.webappPort;
    }

    public String getCommunicationLocation() {
        return this.communicationLocation;
    }

    public String getPlugdescription() {
        return this.plugdescriptionLocation;
    }

    public void initialize() throws IOException {
        Resource confOverride = getOverrideConfigResource();
        File configFile = confOverride.getFile();
        // create override file if it does not exist
        if (!configFile.exists()) {
            // if override file does not exist then try to look for previous
            // communication and plug description to get the configuration
            try {
                log.warn( "No config.override.properties found in conf-directory. Trying to recover "
                        + "configuration from previously generated files." );
                configFile.getParentFile().mkdir();
                configFile.createNewFile();
                // read communicaton and write properties
                this.ibusses = readFromCommunicationXml();
                if (this.ibusses != null)
                    writeCommunicationToProperties();
                // read plug description and write properties
                PlugdescriptionCommandObject pd = new PlugdescriptionCommandObject( new File(
                        "conf/plugdescription.xml" ) );
                writePlugdescriptionToProperties( pd );
            } catch (IOException e1) {
                log.error( "Error creating override configuration", e1 );
                e1.printStackTrace();
            }
        }

        // set system property for use in JSP file!
        if ("true".equals( indexing )) {
            System.setProperty( IKeys.INDEXING, "true" );
        }

        // plug description
        String plugDescription = System.getProperty( IKeys.PLUG_DESCRIPTION );
        if (plugDescription == null) {
            System.setProperty( IKeys.PLUG_DESCRIPTION, plugdescriptionLocation );
        }

        //
        writeCommunication();
    }

    public String getIndexing() {
        return this.indexing;
    }

    /*    public boolean writeConfig(String key, String value) {
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
            // do not remove communication file if no
            if (communicationFile.exists()) {
                communicationFile.delete();
            }
            return true;
        }

        try {
            final XPathService communication = openCommunication( communicationFile );
            Integer id = 0;
            // if server information shall be deleted
            // if (serverName == null) {
            // } else {
            // check if xpath to server exists
            // boolean serverExists = communication.exsistsNode(
            // "/communication/client/connections/server" );

            communication.setAttribute( "/communication/client", "name", this.communicationProxyUrl );

            communication.removeNode( "/communication/client/connections/server", id );
            // create default nodes and attributes if server tag does not exist

            for (CommunicationCommandObject ibus : ibusses) {

                communication.addNode( "/communication/client/connections", "server" );
                communication.addNode( "/communication/client/connections/server", "socket", id );
                communication.addAttribute( "/communication/client/connections/server/socket", "timeout", ""
                        + DEFAULT_TIMEOUT, id );
                communication.addNode( "/communication/client/connections/server", "messages", id );
                communication.addAttribute( "/communication/client/connections/server/messages", "maximumSize", ""
                        + DEFAULT_MAXIMUM_SIZE, id );
                communication.addAttribute( "/communication/client/connections/server/messages", "threadCount", ""
                        + DEFAULT_THREAD_COUNT, id );

                communication.addAttribute( "/communication/client/connections/server", "name",
                        ibus.getBusProxyServiceUrl(), id );
                communication.addAttribute( "/communication/client/connections/server/socket", "port",
                        "" + ibus.getPort(), id );
                communication.addAttribute( "/communication/client/connections/server/socket", "ip", ibus.getIp(), id );
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
        // if (!communicationFile.exists()) {
        final InputStream inputStream = CommunicationConfigurationController.class
                .getResourceAsStream( "/communication-template.xml" );
        communication.registerDocument( inputStream );
        // } else {
        // communication.registerDocument(communicationFile);
        // }

        return communication;
    }

    public void writeCommunicationToProperties() {
        try {
            Resource override = getOverrideConfigResource();
            InputStream is = new FileInputStream( override.getFile().getAbsolutePath() );
            Properties props = new Properties();
            props.load( is );
            // ---------------------------
            props.setProperty( "communication.clientName", communicationProxyUrl );

            String communications = "";
            for (int i = 0; i < ibusses.size(); i++) {
                CommunicationCommandObject ibus = ibusses.get( i );
                communications += ibus.getBusProxyServiceUrl() + "," + ibus.getIp() + "," + ibus.getPort();
                if (i != (ibusses.size() - 1))
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

    @SuppressWarnings("rawtypes")
    public void writePlugdescriptionToProperties(PlugdescriptionCommandObject pd) {
        try {
            // TODO: write all properties to class variables first, to
            // synchronize values!
            // ...

            Resource override = getOverrideConfigResource();
            InputStream is = new FileInputStream( override.getFile().getAbsolutePath() );
            Properties props = new Properties();
            props.load( is );

            for (@SuppressWarnings("unchecked")
            Iterator<String> it = pd.keySet().iterator(); it.hasNext();) {
                String key = it.next();
                Object valObj = pd.get( key );
                if (valObj instanceof String) {
                    props.setProperty( "plugdescription." + key, (String) pd.get( key ) );
                } else if (valObj instanceof List) {
                    props.setProperty( "plugdescription." + key, convertListToString( (List) pd.get( key ) ) );
                } else if (valObj instanceof Integer) {
                    if ("IPLUG_ADMIN_GUI_PORT".equals( key )) {
                        props.setProperty( "jetty.port", String.valueOf( pd.get( key ) ) );
                    } else {
                        props.setProperty( "plugdescription." + key, String.valueOf( pd.get( key ) ) );
                    }
                } else if (valObj instanceof File) {
                    props.setProperty( "plugdescription." + key, ((File) pd.get( key )).getPath() );
                } else {
                    props.setProperty( "plugdescription." + key, pd.get( key ).toString() );
                }
            }

            props.setProperty( "plugdescription.queryExtensions", convertQueryExtensionsToString( this.queryExtensions ) );

            IConfig externalConfig = JettyStarter.getInstance().getExternalConfig();
            if (externalConfig != null) {
                externalConfig.setPropertiesFromPlugdescription( props, pd );
                externalConfig.addPlugdescriptionValues( pd );
            }

            // ---------------------------
            is.close();
            OutputStream os = new FileOutputStream( override.getFile().getAbsolutePath() );
            if (log.isDebugEnabled()) {
                log.debug( "writing configuration to: " + override.getFile().getAbsolutePath() );
            }
            props.store( os, "Override configuration written by the application" );
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addQueryExtensionsToProperties(FieldQueryCommandObject fq) {
        if (this.queryExtensions == null)
            this.queryExtensions = new ArrayList<FieldQueryCommandObject>();
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

    private String convertQueryExtensionsToString(List<FieldQueryCommandObject> queryExtensions) {
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

    private String convertListToString(@SuppressWarnings("rawtypes") List list) {
        return Joiner.on( "," ).join( list );
    }

    public PlugdescriptionCommandObject getPlugdescriptionFromProperties() {
        PlugdescriptionCommandObject pd = new PlugdescriptionCommandObject();

        // working directory
        File pdDir = new File( this.pdWorkingDir );
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

        pd.setOrganisationPartnerAbbr( mainPartner );
        pd.setOrganisationAbbr( mainProvider );
        pd.setOrganisation( organisation );

        pd.setPersonTitle( personTitle );
        pd.setPersonName( personName );
        pd.setPersonSureName( personSurname );
        pd.setPersonMail( personEmail );
        pd.setPersonPhone( personPhone );
        pd.setDataSourceName( datasourceName );
        pd.setDataSourceDescription( datasourceDescription );
        pd.setIplugAdminGuiUrl( guiUrl );
        pd.setIplugAdminGuiPort( this.webappPort );
        pd.setRecordLoader( recordLoader );

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
                addFieldQuery( pd, fq, QUERYTYPE_MODIFY );
            }
        }

        PlugDescriptionUtil.addFieldToPlugDescription( pd, QueryUtil.FIELDNAME_INCL_META );

        for (String field : fields) {
            // if empty property then field can be recognized as empty (bug!!!)
            if (!field.isEmpty())
                PlugDescriptionUtil.addFieldToPlugDescription( pd, field );
        }
        
        if (rankings != null) {
            boolean score = false;
            boolean date = false;
            boolean notRanked = false;
            for (String ranking : rankings) {
                if (ranking.equals( "score" )) {
                    score = true;
                } else if (ranking.equals( "date" )) {
                    date = true;
                } else if (ranking.equals( "off" )) {
                    notRanked = true;
                }
            }

            if (forceAddRankingOff) notRanked = true;
            pd.setRankinTypes( score, date, notRanked );
        }
        pd.putBoolean( "forceAddRankingOff", forceAddRankingOff );
        

        return pd;

    }

    public static void addFieldQuery(final PlugdescriptionCommandObject commandObject,
            final FieldQueryCommandObject fieldQuery, String behaviour) {
        // get container
        QueryExtensionContainer container = commandObject.getQueryExtensionContainer();
        if (null == container) {
            // create container
            container = new QueryExtensionContainer();
            commandObject.setQueryExtensionContainer( container );
        }
        // get extension
        QueryExtension extension = container.getQueryExtension( fieldQuery.getBusUrl() );
        if (null == extension) {
            // create extension
            extension = new QueryExtension();
            extension.setBusUrl( fieldQuery.getBusUrl() );
            container.addQueryExtension( extension );
        }
        // create field query
        final Pattern pattern = Pattern.compile( fieldQuery.getRegex() );
        FieldQuery fq = null;
        if (behaviour.equals( QUERYTYPE_MODIFY )) {
            fq = new FieldQuery( fieldQuery.getRequired(), fieldQuery.getProhibited(), fieldQuery.getKey(),
                    fieldQuery.getValue() );
        } else if (behaviour.equals( QUERYTYPE_DENY )) {
            fq = new FieldQuery( fieldQuery.getRequired(), fieldQuery.getProhibited(), "metainfo", "query_deny" );
            fieldQuery.setKey( "metainfo" );
            fieldQuery.setValue( "query_deny" );
        } else if (behaviour.equals( QUERYTYPE_ALLOW )) {
            fq = new FieldQuery( fieldQuery.getRequired(), fieldQuery.getProhibited(), "metainfo", "query_allow" );
            fieldQuery.setKey( "metainfo" );
            fieldQuery.setValue( "query_allow" );
        }
        extension.addFieldQuery( pattern, fq );
    }

    private List<CommunicationCommandObject> readFromCommunicationXml() {
        // open communication file
        final File communicationFile = new File( "conf/communication.xml" );
        if (!communicationFile.exists()) {
            return null;
        }

        List<CommunicationCommandObject> busses = null;

        // create xpath service for xml
        XPathService communication;
        try {
            communication = new XPathService();
            communication.registerDocument( communicationFile );
            // determine count of ibusses
            final int count = communication.countNodes( "/communication/client/connections/server" );
            this.communicationProxyUrl = communication.parseAttribute( "/communication/client", "name" );

            // create List of communication
            busses = new ArrayList<CommunicationCommandObject>();
            // and get all information about each ibus
            for (int i = 0; i < count; i++) {
                final CommunicationCommandObject bus = new CommunicationCommandObject();
                bus.setBusProxyServiceUrl( communication.parseAttribute( "/communication/client/connections/server",
                        "name", i ) );
                bus.setIp( communication.parseAttribute( "/communication/client/connections/server/socket", "ip", i ) );
                bus.setPort( Integer.parseInt( communication.parseAttribute(
                        "/communication/client/connections/server/socket", "port", i ) ) );
                busses.add( bus );
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // return all busses
        return busses;
    }

    /**
     * Try to get the override configuration first from the classpath and
     * otherwise expect it inside the conf directory. The first option is mainly
     * for development, but should also apply for production since the
     * conf-directory also is in the Classpath. With this function the
     * development environment does not need any manual setup anymore, as long
     * as the test-resources is in the classpath.
     * 
     * @return the resource to the override configuration
     */
    private Resource getOverrideConfigResource() {
        ClassPathResource override = new ClassPathResource( "config.override.properties" );
        try {
            override.getFile();
            return override;
        } catch (FileNotFoundException e) {
            return new FileSystemResource( "conf/config.override.properties" );
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
