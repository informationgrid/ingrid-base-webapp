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
package de.ingrid.admin.elasticsearch;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.NodeBuilder;
import org.springframework.core.io.ClassPathResource;

import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.elasticsearch.converter.DatatypePartnerProviderQueryConverter;
import de.ingrid.admin.elasticsearch.converter.DefaultFieldsQueryConverter;
import de.ingrid.admin.elasticsearch.converter.FieldQueryIGCConverter;
import de.ingrid.admin.elasticsearch.converter.FuzzyQueryConverter;
import de.ingrid.admin.elasticsearch.converter.MatchAllQueryConverter;
import de.ingrid.admin.elasticsearch.converter.QueryConverter;
import de.ingrid.admin.elasticsearch.converter.RangeQueryConverter;
import de.ingrid.admin.elasticsearch.converter.WildcardQueryConverter;
import de.ingrid.admin.service.ElasticsearchNodeFactoryBean;

public class ElasticTests {

    public static ElasticsearchNodeFactoryBean elastic;
    public static QueryConverter qc;
    public static Client client;

    /**
     * This will set up an elastic search environment with an index and some test data,
     * which can be specified.
     * @param index is the name of the index to be created
     * @param fileData is the path to a json file containing the test data for the index
     * @throws Exception
     */
    public static void setup(String index, String fileData, boolean init) throws Exception {
        elastic = new ElasticsearchNodeFactoryBean();
        elastic.afterPropertiesSet();
        client = elastic.getObject().client();

        qc = new QueryConverter();
        List<IQueryParsers> parsers = new ArrayList<IQueryParsers>();
        parsers.add( new DefaultFieldsQueryConverter() );
        parsers.add( new DatatypePartnerProviderQueryConverter() );
        parsers.add( new FieldQueryIGCConverter() );
        parsers.add( new RangeQueryConverter() );
        parsers.add( new WildcardQueryConverter() );
        parsers.add( new FuzzyQueryConverter() );
        parsers.add( new MatchAllQueryConverter() );
        qc.setQueryParsers( parsers );
        
        if (init) {
            JettyStarter.getInstance().config.index = index;
            setMapping( elastic, "test_1" );
            prepareIndex( elastic, fileData, "test_1" );
            ElasticSearchUtils.switchAlias( client, "test_1" );
        }
    }
    
    public static void setup(String index, String fileData) throws Exception {
        setup( index, fileData, true );
    }
    
    public static void setup() throws Exception {
        setup( null, null, false );
    }
    
    private static void prepareIndex(ElasticsearchNodeFactoryBean elastic, String fileData, String index) throws ElasticsearchException, Exception {
        Client client = elastic.getObject().client();
        ClassPathResource resource = new ClassPathResource( fileData );

        byte[] urlsData = Files.readAllBytes( Paths.get( resource.getURI() ) );

        client.prepareBulk().add( urlsData, 0, urlsData.length, true )
                .execute()
                .actionGet();

        // make sure the indexed data is available immediately during search!
        refreshIndex( index, client );
    }

    public static void refreshIndex(String index, Client client) {
        RefreshRequest refreshRequest = new RefreshRequest( index );
        client.admin().indices().refresh( refreshRequest ).actionGet();
    }
    
    private static void setMapping(ElasticsearchNodeFactoryBean elastic, String index) {
        String mappingSource = "";
        try {
            Client client = elastic.getObject().client();
            ClassPathResource resource = new ClassPathResource( "data/mapping.json" );

            List<String> urlsData = Files.readAllLines( Paths.get( resource.getURI() ), Charset.defaultCharset() );
            for (String line : urlsData) {
                mappingSource += line;
            }
            
            if (client.admin().indices().prepareExists(index).execute().actionGet().isExists()) {
                client.admin().indices().prepareDelete(index).execute().actionGet();
            }
            client.admin().indices().prepareCreate(index).execute().actionGet();
            
            client.admin().indices().preparePutMapping().setIndices( index )
                    .setType("base")
                    .setSource( mappingSource )
                    .execute()
                    .actionGet();
            
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public static void createNodeManager() {
        // create a test master node with http support
        NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder();
        nodeBuilder.getSettings().put( "node.master", "false" );
        nodeBuilder.getSettings().put( "http.enabled", "true" );
        nodeBuilder.clusterName( "ingrid" ).data( false ).client( true ).local( false ).node();
    }
}