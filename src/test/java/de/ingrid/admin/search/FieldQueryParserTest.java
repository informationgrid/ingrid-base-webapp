/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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
package de.ingrid.admin.search;

import junit.framework.TestCase;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause.Occur;

import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

public class FieldQueryParserTest extends TestCase {

    public void testRequired() throws Exception {
        FieldQueryParser parser = new FieldQueryParser();
        BooleanQuery booleanQuery = new BooleanQuery();
        IngridQuery ingridQuery = QueryStringParser.parse("+foo:bar");
        parser.parse(ingridQuery, booleanQuery);
        assertEquals(1, booleanQuery.getClauses().length);
        BooleanClause booleanClause = booleanQuery.getClauses()[0];
        assertEquals(Occur.MUST, booleanClause.getOccur());
        assertEquals("+foo:bar", booleanQuery.toString());
    }

    public void testProhibited() throws Exception {
        FieldQueryParser parser = new FieldQueryParser();
        BooleanQuery booleanQuery = new BooleanQuery();
        IngridQuery ingridQuery = QueryStringParser.parse("-foo:bar");
        parser.parse(ingridQuery, booleanQuery);
        assertEquals(1, booleanQuery.getClauses().length);
        BooleanClause booleanClause = booleanQuery.getClauses()[0];
        assertEquals(Occur.MUST_NOT, booleanClause.getOccur());
        assertEquals("-foo:bar", booleanQuery.toString());
    }

}
