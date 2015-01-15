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

import de.ingrid.search.utils.IQueryParser;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

public class TitleQueryParserTest extends TestCase {

    public void testParseTitle() throws Exception {
        IngridQuery ingridQuery = QueryStringParser.parse("foo");
        BooleanQuery booleanQuery = new BooleanQuery();
        IQueryParser parser = new TitleQueryParser(new StandardStemmer());
        parser.parse(ingridQuery, booleanQuery);
        assertEquals(1, booleanQuery.getClauses().length);
        BooleanClause booleanClause = booleanQuery.getClauses()[0];
        assertEquals(Occur.SHOULD, booleanClause.getOccur());
        assertEquals(booleanClause.getQuery().toString(), "title:foo");
    }
}
