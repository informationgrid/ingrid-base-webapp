package de.ingrid.admin.search;

import junit.framework.TestCase;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause.Occur;

import de.ingrid.admin.search.ContentQueryParser;
import de.ingrid.admin.search.IQueryParser;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

public class ContentQueryParserTest extends TestCase {

    public void testParseContent() throws Exception {
        IngridQuery ingridQuery = QueryStringParser.parse("foo");
        BooleanQuery booleanQuery = new BooleanQuery();
        IQueryParser parser = new ContentQueryParser();
        parser.parse(ingridQuery, booleanQuery);
        assertEquals(1, booleanQuery.getClauses().length);
        BooleanClause booleanClause = booleanQuery.getClauses()[0];
        assertEquals(Occur.MUST, booleanClause.getOccur());
        assertEquals(booleanClause.getQuery().toString(), "content:foo");
    }

}
