/*
 * **************************************************-
 * ingrid-iplug-se-iplug
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
package de.ingrid.admin.elasticsearch;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.ingrid.admin.JettyStarter;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

public class GeneralSearchTest extends ElasticTests {

    @BeforeClass
    public static void setUp() throws Exception {
        new JettyStarter( false );
        JettyStarter.getInstance().config.indexFieldSummary = "content";
        setup( "test", "data/webUrls.json" );
        IndexManager indexManager = new IndexManager( elastic );
        indexManager.removeAlias("test");
        indexManager.switchAlias( "test", "test_1" );
    }    
    
   
    @AfterClass
    public static void afterClass() throws Exception {
        elastic.getObject().close();
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
        
        q = QueryStringParser.parse( "au?" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 3 ) );
        Utils.checkHitsForIDs( search.getHits(), 1, 4, 10 );
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
        
        q = QueryStringParser.parse( "au*" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 6 ) );
        Utils.checkHitsForIDs( search.getHits(), 1, 4, 7, 9, 10, 11 );
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
        assertThat( search.length(), is( 1l ) );
        Utils.checkHitsForIDs( search.getHits(), 11 );
    }
    
    @Test
    public void searchFieldOR() throws Exception {
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "content:urlaub OR content:welt" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is( 3l ) );
        Utils.checkHitsForIDs( search.getHits(), 1, 4, 11 );
    }
    
    @Test
    public void searchFieldSpecialAND() throws Exception {
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "partner:bund datatype:pdf" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is( 1l ) );
        Utils.checkHitsForIDs( search.getHits(), 1 );
    }
    
    @Test
    public void searchFieldSpecialOR() throws Exception {
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "datatype:xml OR datatype:pdf" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is( 5l ) );
        Utils.checkHitsForIDs( search.getHits(), 1, 7, 8, 10, 11 );
    }
    
    @Test
    public void searchFieldSpecialORComplex() throws Exception {
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "(datatype:xml OR datatype:pdf) partner:bw" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is( 2l ) );
        Utils.checkHitsForIDs( search.getHits(), 10, 11 );

        q = QueryStringParser.parse( "(datatype:xml OR datatype:pdf) OR partner:bw" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is( 6l ) );
        Utils.checkHitsForIDs( search.getHits(), 10, 11 );
        
        q = QueryStringParser.parse( "(datatype:xml AND partner:bund) OR partner:bw" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is( 4l ) );
        Utils.checkHitsForIDs( search.getHits(), 3, 7, 10, 11 );
        
        q = QueryStringParser.parse( "(datatype:xml AND partner:bund) OR partner:bw Nachrichten" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is( 1l ) );
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
        assertThat( search.length(), is( Long.valueOf( Utils.MAX_RESULTS ) ) );
        assertThat( search.getHits().length, is( 5 ) );
        //Utils.checkHitsForIDs( search.getHits(), 3, 8, 10, 1, 6 );

        search = index.search( q, 5, 5 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is( Long.valueOf( Utils.MAX_RESULTS ) ) );
        assertThat( search.getHits().length, is( 5 ) );
        //Utils.checkHitsForIDs( search.getHits(), 2, 7, 4, 9, 11 );
        
        search = index.search( q, 10, 5 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is( Long.valueOf( Utils.MAX_RESULTS ) ) );
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
        IngridHitDetail detail = index.getDetail( search.getHits()[0], q, new String[] { "url", "fetched" } );
        assertThat( detail, not( is( nullValue() ) ) );
        // assertThat( detail.getHitId(), is( "1" ) );
        assertThat( (String)detail.getArray( IndexImpl.DETAIL_URL )[0], is( "http://www.wemove.com" ) );
        assertThat( (String)detail.getArray("fetched")[0], is( "2014-06-03" ) );
        assertThat( detail.getTitle(), is( "wemove" ) );
        assertThat( detail.getSummary(), is( "Die beste IT-<em>Firma</em> auf der <em>Welt</em>! Preishit" ) );
        assertThat( detail.getScore(), greaterThan( 0.1f ) );
    }
    
    @Test
    public void getCompoundDetail() throws Exception {
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "Preis" );
        IngridHits search = index.search( q, 0, 10 );
        IngridHitDetail detail = index.getDetail( search.getHits()[0], q, new String[] { "url", "fetched" } );
        assertThat( detail, not( is( nullValue() ) ) );
        assertThat( detail.getSummary(), is( "Die beste IT-Firma auf der Welt! <em>Preishit</em>" ) );
        assertThat( detail.getScore(), greaterThan( 0.1f ) );
    }
    
    @Test
    public void getDetailWithRequestedField() throws Exception {
        IndexImpl index = getIndexer();
        IngridQuery q = QueryStringParser.parse( "Welt Firma" );
        IngridHits search = index.search( q, 0, 10 );
        String[] extraFields = new String[] { "url", "fetched" };
        IngridHitDetail detail = index.getDetail( search.getHits()[0], q, extraFields );
        assertThat( detail, not( is( nullValue() ) ) );
        // assertThat( detail.getHitId(), is( "1" ) );
        assertThat( (String)detail.getArray( IndexImpl.DETAIL_URL )[0], is( "http://www.wemove.com" ) );
        assertThat( (String)detail.getArray( "fetched" )[0], is( "2014-06-03" ) );
    }
    

    @Test @Ignore
    public void testDeleteUrl() {
        fail( "Not yet implemented" );
    }
    
}
