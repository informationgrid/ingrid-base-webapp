package de.ingrid.admin.query;

import junit.framework.TestCase;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause.Occur;

import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

public class ContentQueryParserTest extends TestCase {

    public void testParseContent(String query) throws Exception {
        IngridQuery ingridQuery = QueryStringParser.parse("foo");
        BooleanQuery booleanQuery = new BooleanQuery();
        IQueryParser parser = new ContentQueryParser();
        parser.parse(ingridQuery, booleanQuery);
        assert booleanQuery.getClauses().length == 1;
        BooleanClause booleanClause = booleanQuery.getClauses()[0];
        assertEquals(Occur.MUST, booleanClause.getOccur());
        assertEquals(booleanClause.getQuery().toString(), "content:foo");
    }

}
