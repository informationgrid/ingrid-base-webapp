/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
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
import de.ingrid.admin.elasticsearch.ElasticTests;
import de.ingrid.admin.elasticsearch.IndexRunnable;
import de.ingrid.utils.statusprovider.StatusProviderService;
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
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IndexRunnableTest extends ElasticTests {

    private static IndexManager indexManager;
    private IndexRunnable _indexRunnable;

    @Mock
    private PlugDescription _plugDescription;

    @Mock
    PlugDescriptionService pds;

    private ArrayList<IDocumentProducer> docProducers;


    @BeforeAll
    public static void setUp() throws Exception {
        config = new Config();
        config.uuid = "1";
        config.index = "test_1";
        config.indexType = "base";
        config.indexIdFromDoc = "id";
        config.communicationProxyUrl = "/ingrid-group:unit-tests";
        config.datatypes = Arrays.asList("default".split(","));
        setup();

        indexManager = new IndexManager( elastic, elasticConfig );
        indexManager.postConstruct();
    }

    @BeforeEach
    public void beforeTest() {
        /*try {
            elastic.getClient().admin().indices().prepareDelete( "test_1" ).execute().actionGet();
        } catch (Exception ex) {}*/

        MockitoAnnotations.initMocks(this);

        Mockito.when( _plugDescription.getFields() ).thenReturn( new String[] {} );
        Mockito.when( _plugDescription.clone() ).thenReturn( _plugDescription );

        try {
            indexManager.deleteIndex( "test_1" );
        } catch (Exception ignored) {}
        indexManager.createIndex("test_1" );
        setMapping( elastic, "test_1" );

        elasticConfig.indexWithAutoId = false;
    }

    @AfterAll
    public static void afterClass() {
        elastic.getClient().close();
    }

    private void index(int model) throws Exception {
        _indexRunnable = new IndexRunnable(pds, indexManager, null, config, elasticConfig, Optional.empty(), new StatusProviderService());
        _indexRunnable.configure(_plugDescription);
        DummyProducer dummyProducer = new DummyProducer(model);
        dummyProducer.configure(_plugDescription);
        docProducers = new ArrayList<>();
        docProducers.add( dummyProducer );
        _indexRunnable.setDocumentProducers(docProducers);
        _indexRunnable.run();

        try {
            indexManager.refreshIndex( indexManager.getIndexNameFromAliasName(config.index, config.index) );
        } catch (Exception e) {}
        Thread.sleep(1000);
    }

    /**
     * Each document supports its own ID. In case there's an ID twice
     * only the latest document is used. Here we only get 9 of 10 results
     * in the index, because one has an already used identifier.
     * @throws Exception
     */
    @Test
    void indexWithExlusiveId() throws Exception {
        index(0);
        MatchAllQueryBuilder query = QueryBuilders.matchAllQuery();
        //createNodeManager();

        SearchRequestBuilder srb = client.prepareSearch(config.index)
                .setTypes(config.indexType)
                .setQuery(query);
        SearchResponse searchResponse = srb.execute().actionGet();

        SearchHits hitsRes = searchResponse.getHits();
        assertEquals(5, hitsRes.getTotalHits().value);
    }

    @Test
    void indexWithSingleField() throws Exception {
        index(0);
        MatchQueryBuilder query = QueryBuilders.matchQuery("mylist", "first");
        //createNodeManager();

        SearchRequestBuilder srb = client.prepareSearch(config.index)
                .setTypes(config.indexType)
                .storedFields("url", "mylist")
                .setQuery(query);
        SearchResponse searchResponse = srb.execute().actionGet();

        SearchHits hitsRes = searchResponse.getHits();
        SearchHit[] hits = hitsRes.getHits();
        assertEquals(5, hitsRes.getTotalHits().value);
        assertEquals(1, hits[0].field("url").getValues().size());
        assertEquals(1, hits[0].field("mylist").getValues().size());
    }

    @Test
    void indexWithListField() throws Exception {
        index(0);
        MatchQueryBuilder query = QueryBuilders.matchQuery("mylist", "second");
        //createNodeManager();

        SearchRequestBuilder srb = client.prepareSearch(config.index)
                .setTypes(config.indexType)
                .storedFields("url", "mylist")
                .setQuery(query);
        SearchResponse searchResponse = srb.execute().actionGet();

        SearchHits hitsRes = searchResponse.getHits();
        SearchHit[] hits = hitsRes.getHits();
        assertEquals(1, hitsRes.getTotalHits().value);
        assertEquals(1, hits[0].field("url").getValues().size());
        assertEquals(2, hits[0].field("mylist").getValues().size());
    }

    /**
     * In this test the ID of each document is generated automatically, which does
     * not support any update operation, since the documents cannot be identified
     * correctly. Here we get one more result as in the test 'indexWithExlusiveId'
     * because the duplicated ID is ignored.
     * @throws Exception
     */
    @Test
    void indexWithAutoId() throws Exception {
        elasticConfig.indexWithAutoId = true;
        index(0);
        MatchAllQueryBuilder query = QueryBuilders.matchAllQuery();
        //createNodeManager();

        SearchRequestBuilder srb = client.prepareSearch(config.index)
                .setQuery(query);
        SearchResponse searchResponse = srb.execute().actionGet();

        assertEquals(6, searchResponse.getHits().getTotalHits().value);
    }

    @Test
    void testFlipIndex() throws Exception {
        elasticConfig.indexWithAutoId = false;
        IndexImpl index = new IndexImpl( elasticConfig, new IndexManager( elastic, new ElasticConfig() ), qc, new FacetConverter(qc), new QueryBuilderService());
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
    void testGetFacet() throws Exception {
        IndexImpl index = new IndexImpl( elasticConfig, new IndexManager( elastic, elasticConfig ), qc, new FacetConverter(qc), new QueryBuilderService() );
        index(0);
        IngridQuery q = QueryStringParser.parse("title:Marko");
        addFacets(q);

        IngridHits hits = index.search(q, 0, 10);
        assertEquals(1, hits.length());
        assertEquals(1, ((IngridDocument) hits.get("FACETS")).getLong("title:marko"));

    }

    private void addFacets(IngridQuery ingridQuery) {
        IngridDocument f1 = new IngridDocument();
        f1.put("id", "title");

        ingridQuery.put("FACETS", Arrays.asList(new Object[] { f1 }));
    }

}
