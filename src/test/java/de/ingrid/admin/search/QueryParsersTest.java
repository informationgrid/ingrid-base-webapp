package de.ingrid.admin.search;

import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.lucene.search.Query;

import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

public class QueryParsersTest extends TestCase {

    public void testClause() throws Exception {
        QueryParsers transformer = new QueryParsers();
        IQueryParser[] parserArray = new IQueryParser[] { new FieldQueryParser() };
        transformer.setQueryParsers(Arrays.asList(parserArray));
        IngridQuery ingridQuery = QueryStringParser.parse("a:b ((foo:bar) -(bar:foo))");
        Query query = transformer.parse(ingridQuery);
        assertEquals("+(+foo:bar -bar:foo) +a:b", query.toString());
    }

    public void testTermWithDash() throws Exception {
        QueryParsers transformer = new QueryParsers();
        IQueryParser[] parserArray = new IQueryParser[] { new ContentQueryParser(new StandardStemmer()), new TitleQueryParser(new StandardStemmer()), new FieldQueryParserIGC(), new RangeQueryParser(), new WildCardFieldQueryParser(), new AllResultsOnEmptyQueryParser() };
        transformer.setQueryParsers(Arrays.asList(parserArray));
        IngridQuery ingridQuery = QueryStringParser.parse("FFH-Gebiete");
        Query query = transformer.parse(ingridQuery);
        assertEquals("+content:\"ffh gebiete\" title:\"ffh gebiete\"", query.toString());
    }

}
