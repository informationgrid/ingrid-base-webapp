/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.greaterThan;

import org.junit.BeforeClass;
import org.junit.Test;

import de.ingrid.elasticsearch.IndexManager;

public class ElasticSearchUtilsTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {}

    @Test
    public void test() {
        String newName = IndexManager.getNextIndexName( "abc" );
        assertThat( newName, startsWith( "abc_" ) );
        assertThat( newName.length(), greaterThan( 15 ) );
        
        newName = IndexManager.getNextIndexName( "abc_201504141213084" );
        assertThat( newName, startsWith( "abc_" ) );
        assertThat( newName.length(), greaterThan( 15 ) );
        assertThat( newName, not( startsWith( "abc_201504141213084" ) ) );
        
        newName = IndexManager.getNextIndexName( "abc_def" );
        assertThat( newName, startsWith( "abc_def_" ) );
        assertThat( newName.length(), greaterThan( 15 ) );

        newName = IndexManager.getNextIndexName( "abc_def_143" );
        assertThat( newName, startsWith( "abc_def_" ) );
        assertThat( newName, startsWith( "abc_def_143" ) );
        assertThat( newName.length(), greaterThan( 15 ) );
        
        newName = IndexManager.getNextIndexName( "abc_def_201504141213084" );
        assertThat( newName, startsWith( "abc_def_" ) );
        assertThat( newName.length(), greaterThan( 15 ) );
        assertThat( newName, not( startsWith( "abc_def_201504141213084" ) ) );
    }

}
