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

public class Search extends ElasticTests {
    @SuppressWarnings("unused")
    private static Logger log = Logger.getLogger( Search.class );

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        new JettyStarter( false );
        setup( "test", "data/webUrls2.json" );
        ElasticSearchUtils.removeAlias( client, "test_1" );
        ElasticSearchUtils.switchAlias( client, null, "test_1" );
    }
    
    @AfterClass
    public static void afterClass() throws Exception {
        elastic.getObject().close();
    }

    @Test
    public void search() throws Exception {
        
        //createNodeManager();
        
        IndexImpl index2 = new IndexImpl( elastic, qc, new FacetConverter(qc) );
        IngridQuery q = QueryStringParser.parse( "" );
        IngridHits search2 = index2.search( q, 0, 10 );
        assertThat( search2, not( is( nullValue() ) ) );
        assertThat( search2.length(), is( 4l ) );
    }
    
    @Test
    public void getDoc() throws Exception {
        //createNodeManager();
        
        IndexImpl index = new IndexImpl( elastic, qc, new FacetConverter(qc) );
        ElasticDocument response = index.getDocById( "4" );
        assertThat( response, not( is( nullValue() ) ) );
        assertThat( (String)response.get( "url" ), is( "http://www.golemXXX.de" ) );
    }
    
    @Test
    public void searchFacets() throws Exception {
        //createNodeManager();
        
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
    
}
