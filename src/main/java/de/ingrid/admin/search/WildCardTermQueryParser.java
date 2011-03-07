package de.ingrid.admin.search;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.BooleanClause.Occur;

import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.WildCardTermQuery;

/**
 * Maps WildCardTermQuery(s) from IngridQuery to LuceneQuery.
 */
public class WildCardTermQueryParser extends AbstractParser {

//    private static Logger LOG = Logger.getLogger(WildCardTermQueryParser.class);

    private final String _field;
    private Occur _occur;

    public WildCardTermQueryParser(String field, Occur occur) {
        _field = field;
        _occur = occur;
    }

    public void parse(IngridQuery ingridQuery, BooleanQuery booleanQuery) {
        WildCardTermQuery[] wildCardTermQueries = ingridQuery.getWildCardTermQueries();
        for (WildCardTermQuery wildCardTermQuery : wildCardTermQueries) {

        	// how to add new query to boolean query
            Occur occur = null;
            if (_occur != null) {
                occur = _occur;
            } else {
                occur = transform(wildCardTermQuery.isRequred(), wildCardTermQuery.isProhibited());
            }

            WildcardQuery luceneQuery = new WildcardQuery(new Term(_field,
            		wildCardTermQuery.getTerm().toLowerCase()));

            booleanQuery.add(luceneQuery, occur);
        }
    }
}
