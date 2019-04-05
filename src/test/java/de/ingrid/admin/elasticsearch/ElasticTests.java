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
package de.ingrid.admin.elasticsearch;

import de.ingrid.admin.Config;
import de.ingrid.admin.object.IDocumentProducer;
import de.ingrid.admin.service.DummyProducer;
import de.ingrid.elasticsearch.*;
import de.ingrid.elasticsearch.search.FacetConverter;
import de.ingrid.elasticsearch.search.IQueryParsers;
import de.ingrid.elasticsearch.search.IndexImpl;
import de.ingrid.elasticsearch.search.converter.*;
import de.ingrid.utils.xml.XMLSerializer;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.node.Node;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ElasticTests {

    public static ElasticsearchNodeFactoryBean elastic;
    public static QueryConverter qc;
    public static Client client;
    private static List<IDocumentProducer> docProducers;
    public static ElasticConfig elasticConfig;
    protected static Config config;
    protected static IndexManager indexManager;

    /**
     * This will set up an elastic search environment with an index and some test data,
     * which can be specified.
     * @param index is the name of the index to be created
     * @param fileData is the path to a json file containing the test data for the index
     * @throws Exception
     */
    public static void setup(String index, String fileData, boolean init) throws Exception {
        File dir = new File( "./target/test-data" );
        if (dir.exists())
            FileSystemUtils.deleteRecursively( dir );

        Properties elasticProperties = getConfigProperties();
        
        elastic = new ElasticsearchNodeFactoryBean();
        elasticConfig = new ElasticConfig();
        elasticConfig.isEnabled = true;
        IndexInfo[] activeIndices = new IndexInfo[1];
        IndexInfo indexInfo = new IndexInfo();
        indexInfo.setToIndex("test_1");
        indexInfo.setToType("base");
        activeIndices[0] = indexInfo;
        elasticConfig.activeIndices = activeIndices;
        elasticConfig.indexFieldSummary = "content";
        elasticConfig.additionalSearchDetailFields = new String[0];
        elasticConfig.indexSearchDefaultFields = new String[] { "title", "content" };
        elasticConfig.remoteHosts = ((String)elasticProperties.get("elastic.remoteHosts")).split(",");
        elastic.init(elasticConfig);
        elastic.afterPropertiesSet();
        client = elastic.getClient();

        qc = new QueryConverter();
        List<IQueryParsers> parsers = new ArrayList<>();
        parsers.add( new DefaultFieldsQueryConverter(elasticConfig) );
        parsers.add( new DatatypePartnerProviderQueryConverter() );
        parsers.add( new FieldQueryIGCConverter() );
        parsers.add( new RangeQueryConverter() );
        parsers.add( new WildcardQueryConverter() );
        parsers.add( new WildcardFieldQueryConverter() );
        parsers.add( new FuzzyQueryConverter() );
        parsers.add( new MatchAllQueryConverter() );
        qc.setQueryParsers( parsers );
        
        indexManager = new IndexManager( elastic, elasticConfig );
        try {
            indexManager.deleteIndex( "test" );
        } catch (Exception ignored) {}

        if (init) {
//            config.index = index;
//            config.indexType = "base";
            try {
                indexManager.deleteIndex( "test_1" );
            } catch (Exception ignored) {}

            indexManager.createIndex( "test_1" );
            setMapping( elastic, "test_1" );
            prepareIndex( elastic, fileData, "test_1" );
            indexManager.switchAlias( "test", null, "test_1" );
        }
        
        docProducers = new ArrayList<>();
        docProducers.add( new DummyProducer() );
    }

    public static Properties getConfigProperties() {
        Properties p = new Properties();
        try {
            // check for elastic search settings in classpath, which works
            // during development
            // and production
            Resource resource = new ClassPathResource("/config.override.properties");
            if (resource.exists()) {
                p.load(resource.getInputStream());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p;
    }

    public static void setup(String index, String fileData) throws Exception {
        setup( index, fileData, true );
    }
    
    public static void setup() throws Exception {
        setup( null, null, false );
    }
    
    private static void prepareIndex(ElasticsearchNodeFactoryBean elastic, String fileData, String index) throws ElasticsearchException, Exception {
        Client client = elastic.getClient();
        ClassPathResource resource = new ClassPathResource( fileData );

        byte[] urlsData = Files.readAllBytes( Paths.get( resource.getURI() ) );

        client.prepareBulk().add( urlsData, 0, urlsData.length, XContentType.JSON)
                .execute()
                .actionGet();

        // make sure the indexed data is available immediately during search!
        refreshIndex( index, client );
    }

    public static void refreshIndex(String index, Client client) {
        RefreshRequest refreshRequest = new RefreshRequest( index );
        client.admin().indices().refresh( refreshRequest ).actionGet();
    }
    
    public static void deleteIndex(String index, Client client) {
        RefreshRequest refreshRequest = new RefreshRequest( index );
        client.admin().indices().refresh( refreshRequest ).actionGet();
    }
    
    protected static void setMapping(ElasticsearchNodeFactoryBean elastic, String index) {
        try {
            Client client = elastic.getClient();
            ClassPathResource resourceMapping = new ClassPathResource( "data/mapping.json" );
            ClassPathResource resourceSettings = new ClassPathResource( "data/settings.json" );
            String mapping = XMLSerializer.getContents(resourceMapping.getInputStream());
            String settings = XMLSerializer.getContents(resourceSettings.getInputStream());

            if (client.admin().indices().prepareExists(index).execute().actionGet().isExists()) {
                client.admin().indices().prepareDelete(index).execute().actionGet();
            }

            client.admin().indices().prepareCreate(index)
                    .addMapping("base", mapping, XContentType.JSON)
                    .setSettings(settings, XContentType.JSON)
                    .execute().actionGet();
            
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    protected IndexImpl getIndexer() throws Exception {
        IndexImpl indexImpl = new IndexImpl( elasticConfig, indexManager, qc, new FacetConverter(qc), new QueryBuilderService());
//        config.docProducerIndices = new String[] { "test:test" };
        return indexImpl;
    }
    
    @SuppressWarnings("resource")
    public static void createNodeManager() {
        // create a test master node with http support
        Settings settings = Settings.builder()
            .put( "cluster.name", "ingrid" )
            .put( "node.local", "false" )
            .put( "node.data", "false" )
            .put( "node.master", "false" )
            .put( "http.enabled", "false" )
            .put( "node.client", "true" )
            .build();
        new Node(settings);
    }
}
