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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import de.ingrid.admin.elasticsearch.converter.DatatypePartnerProviderQueryConverter;
import de.ingrid.admin.elasticsearch.converter.FieldQueryConverter;
import de.ingrid.admin.elasticsearch.converter.QueryConverter;
import de.ingrid.admin.elasticsearch.converter.WildcardFieldQueryConverter;
import de.ingrid.admin.elasticsearch.converter.WildcardQueryConverter;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.ParseException;
import de.ingrid.utils.queryparser.QueryStringParser;

public class QueriesTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {}

    @Test
    public void queryConvertFields() throws ParseException {
        String query = "(capabilities_url:http* OR t011_obj_serv_op_connpoint.connect_point:http*) datatype:metadata";
        IngridQuery q = QueryStringParser.parse( query );
        QueryConverter queryConverter = new QueryConverter();
        List<IQueryParsers> parsers = new ArrayList<IQueryParsers>();
        parsers.add( new DatatypePartnerProviderQueryConverter() );
        parsers.add( new FieldQueryConverter() );
        parsers.add( new WildcardQueryConverter() );
        parsers.add( new WildcardFieldQueryConverter() );
        queryConverter.setQueryParsers( parsers  );
        BoolQueryBuilder result = queryConverter.convert( q );
        assertThat( result, not( is( nullValue() ) ) );
        assertThat( result.toString(), containsString( "\"query\" : \"http*\"" ) );
        assertThat( result.toString(), containsString( "\"t011_obj_serv_op_connpoint.connect_point\" : {" ) );
    }
    
    @Test
    public void complexOrQuery() throws ParseException {
        String query = "(capabilities_url:http* OR t011_obj_serv_op_connpoint.connect_point:http*) datatype:metadata";
        IngridQuery q = QueryStringParser.parse( query );
        QueryConverter queryConverter = new QueryConverter();
        List<IQueryParsers> parsers = new ArrayList<IQueryParsers>();
        parsers.add( new DatatypePartnerProviderQueryConverter() );
        parsers.add( new FieldQueryConverter() );
        parsers.add( new WildcardQueryConverter() );
        parsers.add( new WildcardFieldQueryConverter() );
        queryConverter.setQueryParsers( parsers  );
        BoolQueryBuilder result = queryConverter.convert( q );
        assertThat( result, not( is( nullValue() ) ) );
        assertThat( result.toString(), containsString( "\"query\" : \"http*\"" ) );
        assertThat( result.toString(), containsString( "\"t011_obj_serv_op_connpoint.connect_point\" : {" ) );
    }

}
