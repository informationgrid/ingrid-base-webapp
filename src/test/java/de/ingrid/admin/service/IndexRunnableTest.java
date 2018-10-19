/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
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
package de.ingrid.admin.service;

import de.ingrid.admin.Config;
import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.elasticsearch.ElasticTests;
import de.ingrid.admin.elasticsearch.IndexRunnable;
import de.ingrid.admin.elasticsearch.StatusProvider;
import de.ingrid.admin.object.IDocumentProducer;
import de.ingrid.elasticsearch.ElasticConfig;
import de.ingrid.elasticsearch.IndexManager;
import de.ingrid.elasticsearch.QueryBuilderService;
import de.ingrid.elasticsearch.search.FacetConverter;
import de.ingrid.elasticsearch.search.IndexImpl;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class IndexRunnableTest extends ElasticTests {

    private IndexRunnable _indexRunnable;

    @Mock
    private PlugDescription _plugDescription;

    private Config config = null;
    
    @Mock
    PlugDescriptionService pds;
    
    private ArrayList<IDocumentProducer> docProducers;
    

    @BeforeClass
    public static void setUp() throws Exception {
        new JettyStarter( false );
        setup();
    }
    
    @Before
    public void beforeTest() throws Exception {
        try {
            elastic.getObject().client().admin().indices().prepareDelete( "test" ).execute().actionGet();
        } catch (IndexNotFoundException ex) {}
        
        MockitoAnnotations.initMocks(this);
        this.config = JettyStarter.getInstance().config;
        config.indexWithAutoId = false;
        
        Mockito.when( _plugDescription.getFields() ).thenReturn( new String[] {} );
    }
    
    @AfterClass
    public static void afterClass() throws Exception {
        elastic.getObject().close();
    }
    
    private void index(int model) throws Exception {
        IndexManager indexManager = new IndexManager( elastic, new ElasticConfig() );
        _indexRunnable = new IndexRunnable(pds, indexManager, null );
        _indexRunnable.configure(_plugDescription);
        _indexRunnable.setStatusProvider( new StatusProvider() );
        DummyProducer dummyProducer = new DummyProducer(model);
        dummyProducer.configure(_plugDescription);
        docProducers = new ArrayList<IDocumentProducer>();
        docProducers.add( dummyProducer );
        _indexRunnable.setDocumentProducers(new ElasticConfig(), docProducers);
        _indexRunnable.run();

        indexManager.refreshIndex( indexManager.getIndexNameFromAliasName(config.index, config.index) );
        Thread.sleep(1000);
    }
    
    /**
     * Each document supports its own ID. In case there's an ID twice
     * only the latest document is used. Here we only get 9 of 10 results
     * in the index, because one has an already used identifier.
     * @throws Exception
     */
    @Test
    public void indexWithExlusiveId() throws Exception {
        index(0);
        MatchAllQueryBuilder query = QueryBuilders.matchAllQuery();
        //createNodeManager();
        
        SearchRequestBuilder srb = client.prepareSearch( config.index )
                .setTypes( config.indexType )
                .setQuery( query );
        SearchResponse searchResponse = srb.execute().actionGet();
        
        SearchHits hitsRes = searchResponse.getHits();
        assertEquals( 5, hitsRes.getTotalHits() );
    }
    
    @Test
    public void indexWithSingleField() throws Exception {
        index(0);
        MatchQueryBuilder query = QueryBuilders.matchQuery( "mylist", "first" );
        //createNodeManager();
        
        SearchRequestBuilder srb = client.prepareSearch( config.index )
                .setTypes( config.indexType )
                .storedFields( "url", "mylist" )
                .setQuery( query );
        SearchResponse searchResponse = srb.execute().actionGet();
        
        SearchHits hitsRes = searchResponse.getHits();
        SearchHit[] hits = hitsRes.getHits();
        assertEquals( 5, hitsRes.getTotalHits() );
        assertEquals( 1, hits[ 0 ].field( "url" ).getValues().size() );
        assertEquals( 1, hits[ 0 ].field( "mylist" ).getValues().size() );
    }
    
    @Test
    public void indexWithListField() throws Exception {
        index(0);
        MatchQueryBuilder query = QueryBuilders.matchQuery( "mylist", "second" );
        //createNodeManager();
        
        SearchRequestBuilder srb = client.prepareSearch( config.index )
                .setTypes( config.indexType )
                .storedFields( "url", "mylist" )
                .setQuery( query );
        SearchResponse searchResponse = srb.execute().actionGet();
        
        SearchHits hitsRes = searchResponse.getHits();
        SearchHit[] hits = hitsRes.getHits();
        assertEquals( 1, hitsRes.getTotalHits() );
        assertEquals( 1, hits[ 0 ].field( "url" ).getValues().size() );
        assertEquals( 2, hits[ 0 ].field( "mylist" ).getValues().size() );
    }
    
    /**
     * In this test the ID of each document is generated automatically, which does
     * not support any update operation, since the documents cannot be identified
     * correctly. Here we get one more result as in the test 'indexWithExlusiveId'
     * because the duplicated ID is ignored.
     * @throws Exception
     */
    @Test
    public void indexWithAutoId() throws Exception {
        config.indexWithAutoId = true;
        index(0);
        MatchAllQueryBuilder query = QueryBuilders.matchAllQuery();
        //createNodeManager();
        
        SearchRequestBuilder srb = client.prepareSearch( config.index )
                .setQuery( query );
        SearchResponse searchResponse = srb.execute().actionGet();
        
        assertEquals( 6, searchResponse.getHits().getTotalHits() );
    }
    
    @Test
    public void testFlipIndex() throws Exception {
        config.indexWithAutoId = true;
        IndexImpl index = new IndexImpl( new ElasticConfig(), new IndexManager( elastic, new ElasticConfig() ), qc, new FacetConverter(qc), new QueryBuilderService());
        index(0);
        IngridQuery q = QueryStringParser.parse("title:Marko");
    	
        long length = index.search(q, 0, 10).length();
        assertEquals(1, length);
        
        index(0);
        length = index.search(q, 0, 10).length();
        assertEquals(1, length);
        
        index(1);        
        length = index.search(q, 0, 10).length();
        assertEquals(2, length);

    }
    
    @Test
    public void testGetFacet() throws Exception {
        IndexImpl index = new IndexImpl( new ElasticConfig(), new IndexManager( elastic, new ElasticConfig() ), qc, new FacetConverter(qc), new QueryBuilderService() );
        index(0);
        IngridQuery q = QueryStringParser.parse("title:Marko");
        addFacets(q);

        IngridHits hits = index.search(q, 0, 10);
        assertEquals(1, hits.length());
        assertEquals(1, ((IngridDocument)hits.get("FACETS")).getLong("title:marko"));
        
    }
    
    private void addFacets(IngridQuery ingridQuery) {
        IngridDocument f1 = new IngridDocument();
        f1.put("id", "title");

        ingridQuery.put("FACETS", Arrays.asList(new Object[] { f1 }));
    }

}
