package de.ingrid.admin.query;

import java.lang.reflect.Method;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

public class TitleQueryParserTest {

    @DataProvider(name = "termQuery")
    public Object[][] createData(Method m) {
        return new Object[][] { new Object[] { "foo" } };
    }

    @Test(dataProvider = "termQuery")
    public void testParseTitle(String query) throws Exception {
        IngridQuery ingridQuery = QueryStringParser.parse(query);
        BooleanQuery booleanQuery = new BooleanQuery();
        IQueryParser parser = new TitleQueryParser();
        parser.parse(ingridQuery, booleanQuery);
        assert booleanQuery.getClauses().length == 1;
        BooleanClause booleanClause = booleanQuery.getClauses()[0];
        assert booleanClause.getOccur() == Occur.SHOULD;
        assert booleanClause.getQuery().toString().equals("title:foo");
    }
}
