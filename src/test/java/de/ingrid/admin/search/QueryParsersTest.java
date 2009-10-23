package de.ingrid.admin.search;

import junit.framework.TestCase;

import org.apache.lucene.search.Query;

import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

public class QueryParsersTest extends TestCase {

    public void testClause() throws Exception {
        QueryParsers transformer = new QueryParsers(new FieldQueryParser());
        IngridQuery ingridQuery = QueryStringParser.parse("a:b ((foo:bar) -(bar:foo))");
        Query query = transformer.parse(ingridQuery);
        assertEquals("+(+foo:bar -bar:foo) +a:b", query.toString());
    }
}
