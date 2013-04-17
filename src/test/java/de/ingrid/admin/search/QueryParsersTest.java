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
