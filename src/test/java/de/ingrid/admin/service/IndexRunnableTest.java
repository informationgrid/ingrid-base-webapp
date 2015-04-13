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
package de.ingrid.admin.service;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.ingrid.admin.Config;
import de.ingrid.admin.IKeys;
import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.TestUtils;
import de.ingrid.admin.elasticsearch.ElasticTests;
import de.ingrid.admin.elasticsearch.IndexRunnable;
import de.ingrid.utils.PlugDescription;

public class IndexRunnableTest extends ElasticTests {

    private IndexRunnable _indexRunnable;

    private PlugDescription _plugDescription;

    private File _file;
    
    private Config config = null;

    @Before
    public void setUp() throws Exception {
        setup( "test", "data/webUrls2.json" );
        //createNodeManager();
        
        _file = new File(System.getProperty("java.io.tmpdir"), this.getClass().getName());
        TestUtils.delete(_file);
       	_file.mkdirs();
        _plugDescription = new PlugDescription();
        _plugDescription.setWorkinDirectory(_file);
        _plugDescription.addDataType("testDataType");
        // store our location of pd as system property to be fetched by pdService
        System.setProperty(IKeys.PLUG_DESCRIPTION, new File(_file.getAbsolutePath(), "plugdescription.xml").getAbsolutePath());
        
        // initialize main program and its config
        new JettyStarter( false );
        config  = JettyStarter.getInstance().config;

//        PowerMockito.mockStatic( JettyStarter.class );
//        Mockito.when(JettyStarter.getInstance()).thenReturn( jettyStarter );
//        
//        Config config = new ConfigBuilder<Config>(Config.class).build();
////        config.communicationProxyUrl = "/ingrid-group:iplug-se-test";
//        jettyStarter.config = Mockito.mock( Config.class );
//        Mockito.stubVoid( jettyStarter.config ).toReturn().on().writePlugdescriptionToProperties(Mockito.any(PlugdescriptionCommandObject.class));
//        
//        QueryParsers transformer = new QueryParsers();
//        IQueryParser[] parserArray = new IQueryParser[] { new FieldQueryParser() };
//        transformer.setQueryParsers(Arrays.asList(parserArray));
//        
//        
//        IngridIndexSearcher searcher = new IngridIndexSearcher(transformer, new LuceneIndexReaderWrapper(null));
//        LuceneIndexReaderWrapper lirw = new LuceneIndexReaderWrapper(null);
//        searcher.setIndexReaderWrapper(lirw);

//        FacetClassProducer fp = new FacetClassProducer();
//        fp.setIndexReaderWrapper(lirw);
//        fp.setQueryParsers(transformer);
//
//        FacetClassRegistry fr = new FacetClassRegistry();
//        fr.setFacetClassProducer(fp);
//
//        IndexFacetCounter fc = new IndexFacetCounter();
//        fc.setFacetClassRegistry(fr);
//
//        FacetManager fm = new FacetManager();
//        fm.setIndexReaderWrapper(lirw);
//        fm.setQueryParsers(transformer);
//        fm.setFacetCounters(Arrays.asList(new IFacetCounter[] { fc }));
//        
//        searcher.setFacetManager(fm);
        
    }
    
    private void index() throws Exception {
        PlugDescriptionService pdService = new PlugDescriptionService();
        _indexRunnable = new IndexRunnable(elastic, pdService);
        _indexRunnable.configure(_plugDescription);
        DummyProducer dummyProducer = new DummyProducer();
        dummyProducer.configure(_plugDescription);
        _indexRunnable.setDocumentProducer(dummyProducer);
        _indexRunnable.run();

        refreshIndex( config.index, client );
    }
    
    private void deleteIndex(String index) {
        client.prepareDeleteByQuery(index).
            setQuery(QueryBuilders.matchAllQuery()).
            //setTypes(indexType).
            execute().actionGet();
        refreshIndex( index, client );
    }

    @After
    public void tearDown() throws Exception {
        TestUtils.delete(_file);
        deleteIndex( config.index );
        elastic.getObject().close();
    }

    /**
     * Each document supports its own ID. In case there's an ID twice
     * only the latest document is used. Here we only get 9 of 10 results
     * in the index, because one has an already used identifier.
     * @throws Exception
     */
    @Test
    public void indexWithExlusiveId() throws Exception {
        index();
        MatchAllQueryBuilder query = QueryBuilders.matchAllQuery();
        //createNodeManager();
        
        SearchRequestBuilder srb = client.prepareSearch( config.index )
                .setTypes( config.indexType )
                .setQuery( query );
        SearchResponse searchResponse = srb.execute().actionGet();
        
        SearchHits hitsRes = searchResponse.getHits();
        assertEquals( 9, hitsRes.getTotalHits() );
    }
    
    @Test
    public void indexWithSingleField() throws Exception {
        index();
        MatchQueryBuilder query = QueryBuilders.matchQuery( "mylist", "first" );
        //createNodeManager();
        
        SearchRequestBuilder srb = client.prepareSearch( config.index )
                .setTypes( config.indexType )
                .addFields( "url", "mylist" )
                .setQuery( query );
        SearchResponse searchResponse = srb.execute().actionGet();
        
        SearchHits hitsRes = searchResponse.getHits();
        SearchHit[] hits = hitsRes.hits();
        assertEquals( 5, hitsRes.getTotalHits() );
        assertEquals( 1, hits[ 0 ].field( "url" ).getValues().size() );
        assertEquals( 1, hits[ 0 ].field( "mylist" ).getValues().size() );
    }
    
    @Test
    public void indexWithListField() throws Exception {
        index();
        MatchQueryBuilder query = QueryBuilders.matchQuery( "mylist", "second" );
        //createNodeManager();
        
        SearchRequestBuilder srb = client.prepareSearch( config.index )
                .setTypes( config.indexType )
                .addFields( "url", "mylist" )
                .setQuery( query );
        SearchResponse searchResponse = srb.execute().actionGet();
        
        SearchHits hitsRes = searchResponse.getHits();
        SearchHit[] hits = hitsRes.hits();
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
        index();
        MatchAllQueryBuilder query = QueryBuilders.matchAllQuery();
        //createNodeManager();
        
        SearchRequestBuilder srb = client.prepareSearch( config.index )
                .setTypes( config.indexType )
                .setQuery( query );
        SearchResponse searchResponse = srb.execute().actionGet();
        
        assertEquals( 10, searchResponse.getHits().getTotalHits() );
    }
    
//    @Test
//    public void testIndexExists() throws IOException {
//        final File file = new File(_file, "index");
//        assertTrue(file.exists());
//        assertTrue(file.list().length > 0);
//        //_indexRunnable.getIngridIndexSearcher().close();
//    }
//
//    @Test
//    public void testReadIndex() throws Exception {
//        final IndexReader reader = IndexReader.open(new File(_file, "index"));
//
//        assertEquals(5, reader.maxDoc());
//
//        assertEquals("Max", reader.document(0).get("first"));
//        assertEquals("08.12.1988", reader.document(0).get("birthdate"));
//
//        assertEquals("Marko", reader.document(1).get("first"));
//        assertEquals("male", reader.document(1).get("gender"));
//
//        assertEquals("Andreas", reader.document(2).get("first"));
//        assertEquals("Kuester", reader.document(2).get("last"));
//
//        assertEquals("Frank", reader.document(3).get("first"));
//        assertNull(reader.document(3).get("nick"));
//
//        assertEquals("öStemmerTestÖ", reader.document(4).get("first"));
//        assertEquals("äStemmerTestÄ", reader.document(4).get("last"));
//        assertEquals("üStemmerTestÜ", reader.document(4).get("gender"));
//        assertEquals("ßStemmerTestß", reader.document(4).get("birthdate"));
//
//        reader.close();
//        
//        _indexRunnable.getIngridIndexSearcher().close();
//        
//    }
//    
//    @Test
//    public void testFlipIndex() throws Exception {
//    	IngridIndexSearcher iis = _indexRunnable.getIngridIndexSearcher();
//    	
//        assertEquals(1, iis.search(QueryStringParser.parse("first:Marko"), 0, 10).length());
//        
//        _indexRunnable.run();
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            fail();
//        }
//        
//        assertEquals(1, iis.search(QueryStringParser.parse("first:Andreas"), 0, 10).length());
//
//        _indexRunnable.getIngridIndexSearcher().close();
//        
//    }
//    
//    @Test
//    public void testGetFacet() throws Exception {
//        IngridIndexSearcher iis = _indexRunnable.getIngridIndexSearcher();
//        
//        IngridQuery q = QueryStringParser.parse("first:Marko");
//        addFacets(q);
//        
//        IngridHits hits = iis.search(q, 0, 10);
//        assertEquals(1, hits.length());
//        assertEquals(1, ((IngridDocument)hits.get("FACETS")).getLong("first:marko"));
//        _indexRunnable.getIngridIndexSearcher().close();
//        
//    }
//    
//    @SuppressWarnings("unchecked")
//    private void addFacets(IngridQuery ingridQuery) {
//        Map f1 = new HashMap();
//        f1.put("id", "first");
//
//        ingridQuery.put("FACETS", Arrays.asList(new Object[] { f1 }));
//    }

}
