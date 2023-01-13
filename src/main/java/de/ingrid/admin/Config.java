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
package de.ingrid.admin;

import de.ingrid.admin.command.CommunicationCommandObject;
import de.ingrid.admin.command.FieldQueryCommandObject;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.controller.CommunicationConfigurationController;
import de.ingrid.elasticsearch.ElasticConfig;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.QueryExtension;
import de.ingrid.utils.QueryExtensionContainer;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.tool.PlugDescriptionUtil;
import de.ingrid.utils.tool.QueryUtil;
import net.weta.components.communication.configuration.ClientConfiguration;
import net.weta.components.communication.configuration.XPathService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

@Configuration
@PropertySource(value = {"classpath:config.properties", "classpath:config.override.properties"})
public class Config {

    private static Log log = LogFactory.getLog( Config.class );

    public static final String QUERYTYPE_ALLOW = "allow";
    public static final String QUERYTYPE_DENY = "deny";
    public static final String QUERYTYPE_MODIFY = "modify";


    private static final List<String> IGNORE_LIST = new ArrayList<>();

    @Autowired(required = false)
    private IConfig externalConfig;

    @Autowired(required = false)
    private ElasticConfig elasticConfig;


    @Value("${iplug.uuid:}")
    public String uuid;

    /**
     * SERVER - CONFIGURATION
     */
    @Value("${jetty.webapp:webapp}")
    public String webappDir;

    @Value("${jetty.port:8082}")
    public Integer webappPort;

    @Value("${communication.server.timeout:10}")
    public int ibusTimeout;
    
    @Value("${communication.server.maxMsgSize:10485760}")
    public int ibusMaxMsgSize;
    
    @Value("${communication.server.threadCount:100}")
    public int ibusThreadCount;

    @Value("${communication.handleTimeout:120}")
    private int iBusHandleTimeout;

    @Value("${communication.queueSize:2000}")
    private int iBusQueueSize;

    /**
     * COMMUNICATION - SETTINGS
     */
    @Value("${communication.location:conf/communication.xml}")
    public String communicationLocation;

    @Value("${communication.clientName:/ingrid-group:base-webapp}")
    public String communicationProxyUrl;

    public List<CommunicationCommandObject> ibusses;

    @Value("${communications.disableIBus:false}")
    public boolean disableIBus;
    

    /**
     * PLUGDESCRIPTION
     */
    @Value("${plugdescription:conf/plugdescription.xml}")
    public String plugdescriptionLocation;

    @Value("${plugdescription.workingDirectory:.}")
    public String pdWorkingDir;

    @Value("${plugdescription.IPLUG_ADMIN_PASSWORD:}")
    public String pdPassword;

    @Value("${indexing:false}")
    public boolean indexing;

    // List and Array behave differently! We have to use split to create a real List object!
    @Value("#{'${plugdescription.dataType:}'.split(',')}")
    public List<String> datatypes;
    
    public Map<String, String[]> datatypesOfIndex = null;

    @Value("${plugdescription.organisationPartnerAbbr:}")
    public String mainPartner;

    @Value("${plugdescription.organisationAbbr:}")
    public String mainProvider;

    @Value("${plugdescription.organisation:}")
    public String organisation;

    @Value("${plugdescription.personTitle:}")
    public String personTitle;

    @Value("${plugdescription.personName:}")
    public String personName;

    @Value("${plugdescription.personSureName:}")
    public String personSurname;

    @Value("${plugdescription.personMail:}")
    public String personEmail;

    @Value("${plugdescription.personPhone:}")
    public String personPhone;

    @Value("${plugdescription.dataSourceName:}")
    public String datasourceName;

    @Value("${plugdescription.dataSourceDescription:}")
    public String datasourceDescription;

    @Value("${plugdescription.IPLUG_ADMIN_GUI_URL:}")
    public String guiUrl;

    @Value("#{'${plugdescription.fields:}'.split(',')}")
    private List<String> fields;

    @Value("${plugdescription.partner:}")
    public String[] partner;

    @Value("${plugdescription.provider:}")
    public String[] provider;

    private List<FieldQueryCommandObject> queryExtensions;
    
    @Value("${plugdescription.isRecordLoader:true}")
    public boolean recordLoader;
    
    @Value("${plugdescription.forceAddRankingOff:false}")
    public boolean forceAddRankingOff;

    @Value("#{'${plugdescription.ranking:off}'.split(',')}")
    public List<String> rankings;

    // used in Utils.java
    @Value("${index.name:test}")
    public String index;

    // used in Utils.java
    @Value("${index.type:base}")
    public String indexType;

    // used in Utils.java
    @Value("${index.alias:}")
    public String indexAlias;

    // used in Utils.java
    @Value("${index.id:id}")
    public String indexIdFromDoc;

    @Value("${index.field.title:title}")
    public String indexFieldTitle;

    @Value("${index.field.summary:summary}")
    public String indexFieldSummary;

    @Value("${search.requested.fields.additional:t02_address.firstname,t02_address.lastname}")
    public String searchRequestedFieldsAdditional;

    // this field is overwritten in iPlugSE, where results
    // shall be grouped by URL instead of the iPlug-ID
    @Value("${index.search.groupByUrl:false}")
    public boolean groupByUrl;

    // this field contains all the index names defined in the doc producers
    // if none was defined there, then the global index from this config is used
    @Value("")
    public String[] docProducerIndices;

    @Value("${index.alwaysCreate:true}")
    public boolean alwaysCreateNewIndex;

    // CACHE - PROPERTIES
    @Value("${plugdescription.CACHED_ELEMENTS:1000}")
    private int cacheElements;
    @Value("${plugdescription.CACHED_IN_DISK_STORE:false}")
    private boolean cacheDiskStore;
    @Value("${plugdescription.CACHED_LIFE_TIME:10}")
    private int cacheLifeTime;
    @Value("${plugdescription.CACHE_ACTIVE:true}")
    private boolean cacheActive;
    
    @Value("${indexOnStartup:false}")
    public boolean indexOnStartup;
    
    @Value("${heartbeatInterval:60}")
    public int heartbeatInterval;

    public ClientConfiguration communicationClientConfiguration;

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
                        "conf/plugdescription.xml" ), this);
                writePlugdescriptionToProperties( pd );
            } catch (IOException e1) {
                log.error( "Error creating override configuration", e1 );
            }
        }

        // set system property for use in JSP file!
        if ( indexing ) {
            System.setProperty( IKeys.INDEXING, "true" );
        }

        // plug description
        String plugDescription = System.getProperty( IKeys.PLUG_DESCRIPTION );
        if (plugDescription == null) {
            System.setProperty( IKeys.PLUG_DESCRIPTION, plugdescriptionLocation );
        }
        
        // ignore the following properties from the plug description, which do not need to be written
        // to the configuration file
        IGNORE_LIST.add( "QUERY_EXTENSION_CONTAINER" );
        IGNORE_LIST.add( "connection" );

        if (!this.disableIBus && this.ibusses != null && this.ibusses.size() > 0) {
            writeCommunication(this.communicationLocation, this.ibusses );
        }

        if (uuid == null || uuid.isEmpty()) {
            uuid = UUID.randomUUID().toString();
            writePlugdescriptionToProperties(new PlugdescriptionCommandObject());

            // also update other configuration object (TODO: this needs refactoring so that we only have one or distinct configurations)
            if (elasticConfig != null) {
                elasticConfig.uuid = uuid;
            }
        }
    }

    public boolean getIndexing() {
        return this.indexing;
    }

    public void writeCommunication(String communicationLocation, List<CommunicationCommandObject> ibusses) {
        File communicationFile = new File( communicationLocation );
        if (ibusses == null || ibusses.isEmpty()) {
            // do not remove communication file if no
            if (communicationFile.exists()) {
                communicationFile.delete();
            }
        }

        try {
            final XPathService communication = openCommunication( communicationFile );
            int id = 0;

            communication.setAttribute( "/communication/client", "name", this.communicationProxyUrl );
            communication.removeNode( "/communication/client/connections/server", id );
            // create default nodes and attributes if server tag does not exist

            for (CommunicationCommandObject ibus : ibusses) {

                communication.addNode( "/communication/client/connections", "server" );
                communication.addNode( "/communication/client/connections/server", "socket", id );
                communication.addAttribute( "/communication/client/connections/server/socket", "timeout", ""
                        + this.ibusTimeout, id );
                communication.addNode( "/communication/client/connections/server", "messages", id );
                communication.addAttribute( "/communication/client/connections/server/messages", "maximumSize", ""
                        + this.ibusMaxMsgSize, id );
                communication.addAttribute( "/communication/client/connections/server/messages", "threadCount", ""
                        + this.ibusThreadCount, id );

                communication.addAttribute( "/communication/client/connections/server", "name",
                        ibus.getBusProxyServiceUrl(), id );
                communication.addAttribute( "/communication/client/connections/server/socket", "port",
                        "" + ibus.getPort(), id );
                communication.addAttribute( "/communication/client/connections/server/socket", "ip", ibus.getIp(), id );
                id++;
            }

            communication.store( communicationFile );

        } catch (Exception e) {
            log.error( "Error writing communication.xml: ", e );
        }

    }

    private XPathService openCommunication(final File communicationFile) throws Exception {
        // first of all create directories if necessary
        if (communicationFile.getParentFile() != null) {
            if (!communicationFile.getParentFile().exists()) {
                communicationFile.getParentFile().mkdirs();
            }
        }

        // open template xml or communication file
        final XPathService communication = new XPathService();
        final InputStream inputStream = CommunicationConfigurationController.class
                .getResourceAsStream( "/communication-template.xml" );
        communication.registerDocument( inputStream );

        return communication;
    }
    
    public Properties getOverrideProperties() throws IOException {
        Resource override = getOverrideConfigResource();
        InputStream is = new FileInputStream( override.getFile().getAbsolutePath() );
        Properties props = new Properties() {
            private static final long serialVersionUID = 6956076060462348684L;
            @Override
            public synchronized Enumeration<Object> keys() {
                return Collections.enumeration(new TreeSet<>(super.keySet()));
            }
        };
        props.load( is );
        return props;
    }

    public void writeCommunicationToProperties() {
        Resource override = getOverrideConfigResource();
        try (InputStream is = new FileInputStream( override.getFile().getAbsolutePath() )) {
            
            Properties props = new Properties() {
                private static final long serialVersionUID = 6956076060462348684L;
                @Override
                public synchronized Enumeration<Object> keys() {
                    return Collections.enumeration(new TreeSet<>(super.keySet()));
                }
            };
            props.load( is );
            // ---------------------------
            props.setProperty( "communication.clientName", communicationProxyUrl );

            StringBuilder communications = new StringBuilder();
            for (int i = 0; i < ibusses.size(); i++) {
                CommunicationCommandObject ibus = ibusses.get( i );
                communications.append(ibus.getBusProxyServiceUrl()).append(",").append(ibus.getIp()).append(",").append(ibus.getPort());
                if (i != (ibusses.size() - 1))
                    communications.append("##");
            }
            props.setProperty( "communications.ibus", communications.toString());

            // ---------------------------
            try (OutputStream os = new FileOutputStream( override.getFile().getAbsolutePath() )) {
                props.store( os, "Override configuration written by the application" );
            }
        } catch (Exception e) {
            log.error( "Error writing properties: " , e );
        }
    }

    public void setQueryExtensions(List<FieldQueryCommandObject> fieldQueries) {
        this.queryExtensions = fieldQueries;
    }

    @SuppressWarnings("rawtypes")
    public void writePlugdescriptionToProperties(PlugdescriptionCommandObject pd) {
        try {

            Resource override = getOverrideConfigResource();
            InputStream is = new FileInputStream( override.getFile().getAbsolutePath() );
            Properties props = new Properties() {
                private static final long serialVersionUID = 6956076060462348684L;
                @Override
                public synchronized Enumeration<Object> keys() {
                    return Collections.enumeration(new TreeSet<>(super.keySet()));
                }
            };
            props.load( is );

            for (Object o : pd.keySet()) {
                String key = (String) o;

                // do not write properties from plug description we do not want
                if (IGNORE_LIST.contains(key)) continue;

                Object valObj = pd.get(key);
                if (valObj instanceof String) {
                    props.setProperty("plugdescription." + key, (String) valObj);
                } else if (valObj instanceof List) {
                    props.setProperty("plugdescription." + key, convertListToString((List) valObj));
                } else if (valObj instanceof Integer) {
                    if ("IPLUG_ADMIN_GUI_PORT".equals(key)) {
                        props.setProperty("jetty.port", String.valueOf(valObj));
                    } else {
                        props.setProperty("plugdescription." + key, String.valueOf(valObj));
                    }
                } else if (valObj instanceof File) {
                    props.setProperty("plugdescription." + key, ((File) valObj).getPath());
                } else {
                    if (valObj != null) {
                        props.setProperty("plugdescription." + key, valObj.toString());
                    } else {
                        log.warn("value of plugdescription field was NULL: " + key);
                    }
                }
            }
            
            // always write working dir as relative path if it was set as such
            String workDir = pd.getRealWorkingDir();
            if (workDir == null) {
                workDir = pd.getWorkinDirectory() == null ? "." : pd.getWorkinDirectory().getPath();
            }
            props.setProperty( "plugdescription.workingDirectory", workDir );

            props.setProperty( "plugdescription.queryExtensions", convertQueryExtensionsToString( this.queryExtensions ) );
            
            props.setProperty( "iplug.uuid", uuid );
            
            setDatatypes(props);

            if (externalConfig != null) {
                externalConfig.setPropertiesFromPlugdescription( props, pd );
                externalConfig.addPlugdescriptionValues( pd );
            }

            // ---------------------------
            is.close();
            try (OutputStream os = new FileOutputStream( override.getFile().getAbsolutePath() )) {
                if (log.isDebugEnabled()) {
                    log.debug( "writing configuration to: " + override.getFile().getAbsolutePath() );
                }
                props.store( os, "Override configuration written by the application" );
            }
        } catch (Exception e) {
            log.error( "Error writing properties:", e );
        }
    }

    private void setDatatypes(Properties props) {
        if (datatypesOfIndex != null) {
            Set<String> indices = datatypesOfIndex.keySet();
            for (String index : indices) {
                props.setProperty( "plugdescription.dataType." + index, String.join( ",", datatypesOfIndex.get( index )));
            }
            // write all collected datatypes, which are transmitted to the iBus
            props.setProperty( "plugdescription.dataType", String.join( ",", datatypes) );
        }
    }

    public void addQueryExtensionsToProperties(FieldQueryCommandObject fq) {
        if (this.queryExtensions == null)
            this.queryExtensions = new ArrayList<>();
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
        StringBuilder result = new StringBuilder();
        if (queryExtensions != null) {
            for (FieldQueryCommandObject fq : queryExtensions) {
                result.append(fq.getBusUrl()).append(",");
                result.append(fq.getRegex()).append(",");
                result.append(fq.getOption()).append(",");
                result.append(fq.getKey()).append(",");
                result.append(fq.getValue()).append(",");
                result.append(fq.getRequired()).append(",");
                result.append(fq.getProhibited()).append("##");
            }
            if (result.length() > 0) {
                return result.substring( 0, result.length() - 2 );
            }
        }
        return result.toString();
    }

    private String convertListToString(List<String> list) {
        return String.join(",", list);
    }

    public PlugdescriptionCommandObject getPlugdescriptionFromConfiguration() {
        PlugdescriptionCommandObject pd = new PlugdescriptionCommandObject();

        // working directory
        File pdDir = new File( this.pdWorkingDir );
        pdDir.mkdirs();
        pd.setWorkinDirectory( pdDir );
        pd.setRealWorkingDir( this.pdWorkingDir );
        pd.setProxyServiceURL( this.communicationProxyUrl );

        pd.remove( PlugDescription.DATA_TYPE );
        if (datatypes != null) {
            for (String datatype : datatypes) {
                pd.addDataType( datatype.trim() );
            }
        }
        if (datatypesOfIndex != null) {
            for (String index : datatypesOfIndex.keySet()) {
                for (String type : datatypesOfIndex.get( index )) {
                    pd.addDatatypesOfIndex( index, type );
                }
            }
        }

        if (!pdPassword.trim().isEmpty()) {
            pd.setIplugAdminPassword( pdPassword );
        }

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
        pd.setCacheActive( cacheActive );
        pd.setCachedLifeTime(cacheLifeTime );
        pd.setCachedElements( cacheElements );
        pd.setCachedInDiskStore( cacheDiskStore );
        // all iPlugs with this version of the base webapp will use the central index unless elasticsearch is disabled
        // in that case the indexing is also probably disabled, e.g. iPlug Opensearch
        pd.put("useRemoteElasticsearch", elasticConfig == null ? false : elasticConfig.isEnabled);
        pd.put("uuid", uuid);
        

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
                switch (ranking) {
                    case "score":
                        score = true;
                        break;
                    case "date":
                        date = true;
                        break;
                    case "off":
                        notRanked = true;
                        break;
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

        switch (behaviour) {
            case QUERYTYPE_MODIFY:
                fq = new FieldQuery(fieldQuery.getRequired(), fieldQuery.getProhibited(), fieldQuery.getKey(),
                        fieldQuery.getValue());
                break;
            case QUERYTYPE_DENY:
                fq = new FieldQuery(fieldQuery.getRequired(), fieldQuery.getProhibited(), "metainfo", "query_deny");
                fieldQuery.setKey("metainfo");
                fieldQuery.setValue("query_deny");
                break;
            case QUERYTYPE_ALLOW:
                fq = new FieldQuery(fieldQuery.getRequired(), fieldQuery.getProhibited(), "metainfo", "query_allow");
                fieldQuery.setKey("metainfo");
                fieldQuery.setValue("query_allow");
                break;
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
            busses = new ArrayList<>();
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
            log.error( "Error when reading from communication.xml", e );
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
            // do nothing here! get file from conf directory (see return value)
        } catch (IOException e) {
            log.error( "Error when getting config.override.properties", e );
        }
        return new FileSystemResource( "conf/config.override.properties" );
    }

    @Value("${communications.ibus:}")
    private void setCommunication(String ibusse) {
        List<CommunicationCommandObject> list = new ArrayList<>();
        List<ClientConfiguration.ClientConnection> clients = new ArrayList<>();
        String[] split = ibusse.split( "##" );
        for (String comm : split) {
            String[] communication = comm.split( "," );
            if (communication.length == 3) {
                CommunicationCommandObject commObject = new CommunicationCommandObject();
                commObject.setBusProxyServiceUrl( communication[0] );
                commObject.setIp( communication[1] );
                commObject.setPort( Integer.valueOf( communication[2] ) );
                list.add( commObject );

                ClientConfiguration.ClientConnection clientConnection = new ClientConfiguration().new ClientConnection();
                clientConnection.setServerName(communication[0]);
                clientConnection.setServerIp(communication[1]);
                clientConnection.setServerPort(Integer.parseInt(communication[2]));
                clientConnection.setMaxMessageSize(ibusMaxMsgSize);
                clientConnection.setMessageThreadCount(ibusThreadCount);
                clientConnection.setSocketTimeout(ibusTimeout);

                clients.add(clientConnection);
            }
        }

        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setHandleTimeout(iBusHandleTimeout);
        clientConfiguration.setQueueSize(iBusQueueSize);
        clientConfiguration.setClientConnections(clients);

        ibusses = list;
        this.communicationClientConfiguration = clientConfiguration;
    }

    @Value("${plugdescription.queryExtensions:}")
    private void setFieldQueries(String queries) {
        List<FieldQueryCommandObject> list = new ArrayList<>();
        if (!"".equals( queries )) {
            String[] split = queries.split("##");
            for (String extensions : split) {
                String[] extArray = extensions.split(",");
                if (extArray.length == 7) {
                    FieldQueryCommandObject commObject = new FieldQueryCommandObject();
                    commObject.setBusUrl(extArray[0]);
                    commObject.setRegex(extArray[1]);
                    commObject.setOption(extArray[2]);
                    commObject.setKey(extArray[3]);
                    commObject.setValue(extArray[4]);
                    if ("true".equals(extArray[5])) {
                        commObject.setRequired();
                    }
                    if ("true".equals(extArray[6])) {
                        commObject.setProhibited();
                    }
                    list.add(commObject);
                } else {
                    log.error("QueryExtension could not be extracted, because of missing values. Expected 7 but got: "
                            + extArray.length);
                }
            }
        }

        queryExtensions = list;
    }
}
