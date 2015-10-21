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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.ingrid.admin.JettyStarter;
import de.ingrid.utils.ElasticDocument;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

public class SearchTest extends ElasticTests {
    @SuppressWarnings("unused")
    private static Logger log = Logger.getLogger( SearchTest.class );

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        new JettyStarter( false );
        setup( "test", "data/webUrls2.json" );
        ElasticSearchUtils.removeAlias( client );
        ElasticSearchUtils.switchAlias( client, "test_1" );
    }
    
    @AfterClass
    public static void afterClass() throws Exception {
        elastic.getObject().close();
    }

    @Test
    public void search() throws Exception {
        
        IndexImpl index2 = new IndexImpl( elastic, qc, new FacetConverter(qc) );
        IngridQuery q = QueryStringParser.parse( "" );
        IngridHits search2 = index2.search( q, 0, 10 );
        assertThat( search2, not( is( nullValue() ) ) );
        assertThat( search2.length(), is( 4l ) );
    }
    
    @Test
    public void searchComplex() throws Exception {
        
        IndexImpl index2 = new IndexImpl( elastic, qc, new FacetConverter(qc) );
        IngridQuery q = QueryStringParser.parse( "(title:Mathematics OR title:Prime) AND (title:Square OR title:Prime)" );
        IngridHits search2 = index2.search( q, 0, 10 );
        assertThat( search2, not( is( nullValue() ) ) );
        assertThat( search2.length(), is( 1l ) );

        q = QueryStringParser.parse( "(title:Mathematics OR title:Prime) AND (partner:bw OR partner:bund)" );
        search2 = index2.search( q, 0, 10 );
        assertThat( search2, not( is( nullValue() ) ) );
        assertThat( search2.length(), is( 2l ) );

        q = QueryStringParser.parse( "(title:Mathematics OR title:Prime) OR (title:Square OR title:Prime)" );
        search2 = index2.search( q, 0, 10 );
        assertThat( search2, not( is( nullValue() ) ) );
        assertThat( search2.length(), is( 3l ) );
        
        q = QueryStringParser.parse( "(title:Mathematics OR title:Prime) OR (title:Square)" );
        search2 = index2.search( q, 0, 10 );
        assertThat( search2, not( is( nullValue() ) ) );
        assertThat( search2.length(), is( 3l ) );
    }
    
    @Test
    public void searchTwoFields() throws Exception {
        IndexImpl index2 = new IndexImpl( elastic, qc, new FacetConverter(qc) );
        IngridQuery q = QueryStringParser.parse( "title:Mathematics partner:bund" );
        IngridHits search2 = index2.search( q, 0, 10 );
        assertThat( search2, not( is( nullValue() ) ) );
        assertThat( search2.length(), is( 1l ) );
        
        q = QueryStringParser.parse( "title:Mathematics partner:bw" );
        search2 = index2.search( q, 0, 10 );
        assertThat( search2, not( is( nullValue() ) ) );
        assertThat( search2.length(), is( 0l ) );
        
        q = QueryStringParser.parse( "title:Mathematics -partner:bw" );
        search2 = index2.search( q, 0, 10 );
        assertThat( search2, not( is( nullValue() ) ) );
        assertThat( search2.length(), is( 1l ) );
        
        q = QueryStringParser.parse( "title:Mathematics -partner:bund" );
        search2 = index2.search( q, 0, 10 );
        assertThat( search2, not( is( nullValue() ) ) );
        assertThat( search2.length(), is( 0l ) );
    }
    
    @Test
    public void getDoc() throws Exception {
        
        IndexImpl index = new IndexImpl( elastic, qc, new FacetConverter(qc) );
        ElasticDocument response = index.getDocById( "4" );
        assertThat( response, not( is( nullValue() ) ) );
        assertThat( (String)response.get( "url" ), is( "http://www.golemXXX.de" ) );
    }
    
    @Test
    public void searchFacets() throws Exception {
        
        List<IngridDocument> facetQueries = new ArrayList<IngridDocument>();
        IngridDocument faceteEntry = new IngridDocument();
        faceteEntry.put("id", "datatype");
        faceteEntry.put("field", "datatype");
        facetQueries.add(faceteEntry);
        
        IndexImpl index = new IndexImpl( elastic, qc, new FacetConverter(qc) );
        IngridQuery q = QueryStringParser.parse( "" );
        q.put("FACETS", facetQueries);
        
        IngridHits search2 = index.search( q, 0, 10 );
        assertThat( search2, not( is( nullValue() ) ) );
        IngridDocument facets = (IngridDocument) search2.get( "FACETS" );
        assertThat( facets.keySet().size(), is( 2 ) );
        assertThat( facets.keySet().contains( "datatype:www"), is( true ) );
        assertThat( facets.get( "datatype:www").toString(), is( "3" ) );
        assertThat( facets.keySet().contains( "datatype:pdf"), is( true ) );
        assertThat( facets.get( "datatype:pdf").toString(), is( "1" ) );
    }
    
    /**
     * When searching for more than one term, then the terms should be in title and summary
     * but not necessarily all in the same field. It's just important that the document contains
     * those terms.
     * @throws Exception
     */
    @Test
    public void searchTitleAndContent() throws Exception {
        
        IndexImpl index = new IndexImpl( elastic, qc, new FacetConverter(qc) );
        // both terms are found in the content field => one match
        IngridQuery q = QueryStringParser.parse( "biggest number" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is( 1l ) );
        
        // one term is found in title, the other in content => one match
        q = QueryStringParser.parse( "four rectangle" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is( 1l ) );
        
        // one term is found in title, the other nowhere => no match
        q = QueryStringParser.parse( "impossible rectangle" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is( 0l ) );
    }
    
}