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
package de.ingrid.admin.search;

import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.lucene.search.Query;

import de.ingrid.search.utils.IQueryParser;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

public class QueryParsersTest extends TestCase {

    Stemmer defaultStemmer = AbstractParser.getDefaultStemmer();

    public void testClause() throws Exception {
        QueryParsers transformer = new QueryParsers();
        IQueryParser[] parserArray = new IQueryParser[] { new FieldQueryParser() };
        transformer.setQueryParsers(Arrays.asList(parserArray));
        IngridQuery ingridQuery = QueryStringParser.parse("a:b ((foo:bar) -(bar:foo))");
        Query query = transformer.parse(ingridQuery);
        assertEquals("+(+foo:bar -bar:foo) +a:b", query.toString());
    }

    public void testTermWithDashAndUmlaute() throws Exception {
        QueryParsers transformer = new QueryParsers();
        IQueryParser[] parserArray = new IQueryParser[] { new ContentQueryParser(defaultStemmer), new TitleQueryParser(defaultStemmer), new FieldQueryParserIGC(), new RangeQueryParser(), new WildCardFieldQueryParser(), new AllResultsOnEmptyQueryParser() };
        transformer.setQueryParsers(Arrays.asList(parserArray));
        IngridQuery ingridQuery = QueryStringParser.parse("FFH-Gebiete-öTestÖ-äTestÄ-üTestÜ-ßßß");
        Query query = transformer.parse(ingridQuery);
        assertEquals("+content:\"ffh gebie otesto atesta utestu ssssss\" title:\"ffh gebie otesto atesta utestu ssssss\"", query.toString());
    }

    public void testTermWithIS() throws Exception {
        QueryParsers transformer = new QueryParsers();
        IQueryParser[] parserArray = new IQueryParser[] { new ContentQueryParser(defaultStemmer), new TitleQueryParser(defaultStemmer), new FieldQueryParserIGC(), new RangeQueryParser(), new WildCardFieldQueryParser(), new AllResultsOnEmptyQueryParser() };
        transformer.setQueryParsers(Arrays.asList(parserArray));
        IngridQuery ingridQuery = QueryStringParser.parse("is bk 50");
        Query query = transformer.parse(ingridQuery);
        assertEquals("+content:is +content:bk +content:50 title:is title:bk title:50", query.toString());
    }

}
