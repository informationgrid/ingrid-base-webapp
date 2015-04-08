package de.ingrid.admin.elasticsearch;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import de.ingrid.admin.JettyStarter;
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
    }

    @Test
    public void search() throws Exception {
        
        //createNodeManager();
        
        IndexImpl index2 = new IndexImpl( elastic, qc, new FacetConverter() );
        IngridQuery q = QueryStringParser.parse( "" );
        IngridHits search2 = index2.search( q, 0, 10 );
        assertThat( search2, not( is( nullValue() ) ) );
        assertThat( search2.length(), is( 4l ) );
    }
    
    @Test
    public void getDoc() throws Exception {
        //createNodeManager();
        
        IndexImpl index = new IndexImpl( elastic, qc, new FacetConverter() );
        Map<String, Object> response = index.getDocById( "4" );
        assertThat( response, not( is( nullValue() ) ) );
        assertThat( (String)response.get( "url" ), is( "http://www.golemXXX.de" ) );
    }
    
}
