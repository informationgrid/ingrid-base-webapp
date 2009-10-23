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
