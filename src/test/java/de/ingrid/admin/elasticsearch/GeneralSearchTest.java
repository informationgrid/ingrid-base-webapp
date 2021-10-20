/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
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
import de.ingrid.elasticsearch.IndexManager;
import de.ingrid.elasticsearch.search.IndexImpl;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class GeneralSearchTest extends ElasticTests {

    @BeforeClass
    public static void setUp() throws Exception {
        config = new Config();
//        config.indexFieldTitle = "title";
//        config.indexFieldSummary = "content";
//        config.additionalSearchDetailFields = new String[] {"url", "title", "partner", "datatype", "content", "fetched", "iPlugId"};
        // new JettyStarter(false);


        setup( "test", "data/webUrls.json" );

        elasticConfig.indexFieldTitle = "title";
        elasticConfig.indexFieldSummary = "content";
        elasticConfig.additionalSearchDetailFields = new String[] {"url", "title", "partner", "datatype", "content", "fetched", "iPlugId"};

        IndexManager indexManager = new IndexManager( elastic, elasticConfig );
        indexManager.removeFromAlias("test", "test_1");
        indexManager.switchAlias( "test", null, "test_1" );
    }


    @AfterClass
    public static void afterClass() {
        // elastic.getObject().close();
    }

    @Test
    public void searchForOneTerm() throws Exception {
        //elastic.getObject().client().settings().
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "wemove" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 4 ) );
        Utils.checkHitsForIDs( search.getHits(), 1, 6, 7, 8 );
    }

    @Test
    public void searchForMultipleTermsWithAnd() throws Exception {
        IndexImpl index = getIndexer();
        // both words must be present inside a field!
        IngridQuery q = QueryStringParser.parse( "Welt Neuigkeit" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 4 );
    }

    @Test
    public void searchForMultipleTermsWithOr() throws Exception {
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "wemove OR reisen" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 5 ) );
        Utils.checkHitsForIDs( search.getHits(), 1, 6, 7, 8, 11 );

        q = QueryStringParser.parse( "((wemove) OR (reisen))" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 5 ) );
        Utils.checkHitsForIDs( search.getHits(), 1, 6, 7, 8, 11 );
    }

    /*
     * Show me all docs containing (Welt AND wemove) plus every doc
     * containing "golem".
     */
    @Test
    public void searchForMultipleTermsWithAndOr() throws Exception {
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "Welt AND Firma OR golem" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 3 ) );
        Utils.checkHitsForIDs( search.getHits(), 1, 4, 5 );
    }

    @Test
    public void searchForMultipleTermsWithAndOrParentheses() throws Exception {
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "Welt AND (Firma OR golem)" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 2 ) );
        Utils.checkHitsForIDs( search.getHits(), 1, 4 );
    }

    @Test
    public void searchForTermNot() throws Exception {
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "-wemove" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( (int)Utils.MAX_RESULTS - 4 ) );
        Utils.checkHitsForIDs( search.getHits(), 2, 3, 4, 5, 9, 10, 11 );
    }

    @Test
    public void searchForMultipleTermsNot() throws Exception {
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "Welt -Firma" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 2 ) );
        Utils.checkHitsForIDs( search.getHits(), 4, 11 );
    }

    @Test
    public void searchWithWildcardCharacter() throws Exception {
        IndexImpl index = getIndexer();
        // the term Deutschland should be found
        IngridQuery q = QueryStringParser.parse( "Deutschl?nd" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 9 );

        // should not find the following, because only one character is a wildcard!
        q = QueryStringParser.parse( "Deutschl?d" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 0 ) );

        q = QueryStringParser.parse( "wel?" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 3 ) );
        Utils.checkHitsForIDs( search.getHits(), 1, 4, 11 );
    }

    @Test
    public void searchWithWildcardString() throws Exception {
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "Deutschl*nd" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 9 );

        q = QueryStringParser.parse( "Deutschl*d" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 9 );

        q = QueryStringParser.parse( "st*" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 3 ) );
        Utils.checkHitsForIDs( search.getHits(), 2, 6, 8 );
    }

    @Test
    public void searchCombinedWildcards() throws Exception {
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "Deutschl*nd OR Entstehung" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 2 ) );
        Utils.checkHitsForIDs( search.getHits(), 6, 9 );

        q = QueryStringParser.parse( "(Deutschl*nd OR Entstehung)" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 2 ) );
        Utils.checkHitsForIDs( search.getHits(), 6, 9 );

        q = QueryStringParser.parse( "(Deutschl*nd OR Ents*ung)" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 2 ) );
        Utils.checkHitsForIDs( search.getHits(), 6, 9 );

        q = QueryStringParser.parse( "(Deutschl*nd OR Ents*ung) title:wemove" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 6 );
    }

    @Test
    public void searchFuzzy() throws Exception {
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "Deutschlnad" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 0 ) );

        q = QueryStringParser.parse( "Deutschlnad~" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 9 );
    }

    @Test
    public void searchFuzzyCombination() throws Exception {
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "faxen Deutschlnad~" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 9 );

        q = QueryStringParser.parse( "wemove -Wetl~" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 3 ) );
        Utils.checkHitsForIDs( search.getHits(), 6, 7, 8 );
    }

    @Test
    public void searchField() throws Exception {
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "title:ausland" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 9 );
    }

    @Test
    public void searchFieldAND() throws Exception {
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "content:urlaub content:welt" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is(1L) );
        Utils.checkHitsForIDs( search.getHits(), 11 );
    }

    @Test
    public void searchFieldOR() throws Exception {
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "content:urlaub OR content:welt" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is(3L) );
        Utils.checkHitsForIDs( search.getHits(), 1, 4, 11 );
    }

    @Test
    public void searchFieldSpecialAND() throws Exception {
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "partner:bund datatype:pdf" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is(1L) );
        Utils.checkHitsForIDs( search.getHits(), 1 );
    }

    @Test
    public void searchFieldSpecialOR() throws Exception {
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "datatype:xml OR datatype:pdf" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is(5L) );
        Utils.checkHitsForIDs( search.getHits(), 1, 7, 8, 10, 11 );
    }

    @Test
    public void searchFieldSpecialORComplex() throws Exception {
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "(datatype:xml OR datatype:pdf) partner:bw" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is(2L) );
        Utils.checkHitsForIDs( search.getHits(), 10, 11 );

        q = QueryStringParser.parse( "(datatype:xml OR datatype:pdf) OR partner:bw" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is(6L) );
        Utils.checkHitsForIDs( search.getHits(), 10, 11 );

        q = QueryStringParser.parse( "(datatype:xml AND partner:bund) OR partner:bw" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is(4L) );
        Utils.checkHitsForIDs( search.getHits(), 3, 7, 10, 11 );

        q = QueryStringParser.parse( "(datatype:xml AND partner:bund) OR partner:bw Nachrichten" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is(1L) );
        Utils.checkHitsForIDs( search.getHits(), 3);
    }

    @Test
    public void searchPhrase() throws Exception {
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "\"der Wirtschaft\"" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 10 );

        q = QueryStringParser.parse( "\"Welt der Computer\"" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 4 );
    }

    @Test
    public void stopWordsRemoval() throws Exception {
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "Welt das ein Computer" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 2 ) );
        Utils.checkHitsForIDs( search.getHits(), 4, 11 );
    }

    @Test
    public void searchWithPaging() throws Exception {
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "" );
        IngridHits search = index.search( q, 0, 5 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is(Utils.MAX_RESULTS) );
        assertThat( search.getHits().length, is( 5 ) );
        //Utils.checkHitsForIDs( search.getHits(), 3, 8, 10, 1, 6 );

        search = index.search( q, 5, 5 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is(Utils.MAX_RESULTS) );
        assertThat( search.getHits().length, is( 5 ) );
        //Utils.checkHitsForIDs( search.getHits(), 2, 7, 4, 9, 11 );

        search = index.search( q, 10, 5 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is(Utils.MAX_RESULTS) );
        assertThat( search.getHits().length, is( 1 ) );
        //Utils.checkHitsForIDs( search.getHits(), 5 );
    }

    @Test @Ignore
    public void searchForTermDateLocation() {
        fail( "Not yet implemented" );
    }

    @Test
    public void getDetail() throws Exception {
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "Welt Firma" );
        IngridHits search = index.search( q, 0, 10 );
        String[] extraFields = new String[] { elasticConfig.indexFieldTitle, elasticConfig.indexFieldSummary, "url", "fetched" };
        IngridHitDetail detail = index.getDetail( search.getHits()[0], q, extraFields );
        assertThat( detail, not( is( nullValue() ) ) );
        // assertThat( detail.getHitId(), is( "1" ) );
        assertThat(detail.get( "url" ), is( "http://www.wemove.com" ) );
        assertThat(detail.get("fetched"), is( "2014-06-03" ) );
        assertThat( detail.getTitle(), is( "wemove" ) );
        assertThat( detail.getSummary(), is( "Die beste IT-<em>Firma</em> auf der <em>Welt</em>! Preishit" ) );
        assertThat( detail.getScore(), greaterThan( 0.1f ) );
    }

    @Test
    public void getCompoundDetail() throws Exception {
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "Preis" );
        IngridHits search = index.search( q, 0, 10 );

        assertThat( search.getHits().length, is( greaterThan(0) ) );

        String[] extraFields = new String[] { elasticConfig.indexFieldTitle, elasticConfig.indexFieldSummary, "url", "fetched" };
        IngridHitDetail detail = index.getDetail( search.getHits()[0], q, extraFields );
        assertThat( detail, not( is( nullValue() ) ) );
//        assertThat( detail.getSummary(), is( "Die beste IT-Firma auf der Welt! <em>Preishit</em>" ) );
        assertThat( detail.getSummary(), is( "<em>Preishit</em>" ) );
        assertThat( detail.getScore(), greaterThan( 0.1f ) );
    }

    @Test
    public void getDetailWithRequestedField() throws Exception {
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "Welt Firma" );
        IngridHits search = index.search( q, 0, 10 );
        String[] extraFields = new String[] { elasticConfig.indexFieldTitle, elasticConfig.indexFieldSummary, "url", "fetched" };
        IngridHitDetail detail = index.getDetail( search.getHits()[0], q, extraFields );
        assertThat( detail, not( is( nullValue() ) ) );
        // assertThat( detail.getHitId(), is( "1" ) );
//       TODO: why is it not an array anymore? -> assertThat(detail.getArray( "url" )[0], is( "http://www.wemove.com" ) );
        assertThat(detail.get( "url" ), is( "http://www.wemove.com" ) );
//        TODO: same here -> assertThat(detail.getArray( "fetched" )[0], is( "2014-06-03" ) );
        assertThat(detail.get( "fetched" ), is( "2014-06-03" ) );
    }


    @Test @Ignore
    public void testDeleteUrl() {
        fail( "Not yet implemented" );
    }

}
