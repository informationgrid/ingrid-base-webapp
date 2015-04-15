package de.ingrid.admin.elasticsearch;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.greaterThan;

import org.junit.BeforeClass;
import org.junit.Test;

public class ElasticSearchUtilsTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {}

    @Test
    public void test() {
        String newName = ElasticSearchUtils.getNextIndexName( "abc" );
        assertThat( newName, startsWith( "abc_" ) );
        assertThat( newName.length(), greaterThan( 15 ) );
        
        newName = ElasticSearchUtils.getNextIndexName( "abc_201504141213084" );
        assertThat( newName, startsWith( "abc_" ) );
        assertThat( newName.length(), greaterThan( 15 ) );
        assertThat( newName, not( startsWith( "abc_201504141213084" ) ) );
        
        newName = ElasticSearchUtils.getNextIndexName( "abc_def" );
        assertThat( newName, startsWith( "abc_def_" ) );
        assertThat( newName.length(), greaterThan( 15 ) );

        newName = ElasticSearchUtils.getNextIndexName( "abc_def_143" );
        assertThat( newName, startsWith( "abc_def_" ) );
        assertThat( newName, startsWith( "abc_def_143" ) );
        assertThat( newName.length(), greaterThan( 15 ) );
        
        newName = ElasticSearchUtils.getNextIndexName( "abc_def_201504141213084" );
        assertThat( newName, startsWith( "abc_def_" ) );
        assertThat( newName.length(), greaterThan( 15 ) );
        assertThat( newName, not( startsWith( "abc_def_201504141213084" ) ) );
    }

}
