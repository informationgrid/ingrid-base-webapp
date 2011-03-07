package de.ingrid.admin.search;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.springframework.stereotype.Service;

import de.ingrid.utils.query.FuzzyFieldQuery;
import de.ingrid.utils.query.IngridQuery;

/**
 * Generic mapping of FuzzyFieldQuery(s) from IngridQuery to LuceneQuery.
 */
@Service
public class FuzzyFieldQueryParser extends AbstractParser {

    @Override
    public void parse(IngridQuery ingridQuery, BooleanQuery booleanQuery) {
        FuzzyFieldQuery[] fuzzyFieldQueries = ingridQuery.getFuzzyFieldQueries();
        for (FuzzyFieldQuery fuzzyFieldQuery : fuzzyFieldQueries) {
            FuzzyQuery luceneQuery =
            	new FuzzyQuery(new Term(fuzzyFieldQuery.getFieldName(), fuzzyFieldQuery.getFieldValue()));

            booleanQuery.add(luceneQuery, transform(fuzzyFieldQuery.isRequred(), fuzzyFieldQuery.isProhibited()));
        }
    }
}
