package de.ingrid.admin.search;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.WildcardQuery;
import org.springframework.stereotype.Service;

import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.WildCardFieldQuery;

/**
 * Generic mapping of WildCardFieldQuery(s) from IngridQuery to LuceneQuery.
 */
@Service
public class WildCardFieldQueryParser extends AbstractParser {

    @Override
    public void parse(IngridQuery ingridQuery, BooleanQuery booleanQuery) {
        WildCardFieldQuery[] wildCardFieldQueries = ingridQuery.getWildCardFieldQueries();
        for (WildCardFieldQuery wildCardQuery : wildCardFieldQueries) {
        	WildcardQuery luceneQuery =
        		new WildcardQuery(new Term(wildCardQuery.getFieldName(), wildCardQuery.getFieldValue()));

            booleanQuery.add(luceneQuery, transform(wildCardQuery.isRequred(), wildCardQuery.isProhibited()));
        }
    }
}
