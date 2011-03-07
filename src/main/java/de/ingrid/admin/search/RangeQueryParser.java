package de.ingrid.admin.search;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.springframework.stereotype.Service;

import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.RangeQuery;

/**
 * Generic mapping of RangeQuery(s) from IngridQuery to LuceneQuery.
 */
@Service
public class RangeQueryParser extends AbstractParser {

    @Override
    public void parse(IngridQuery ingridQuery, BooleanQuery booleanQuery) {
        RangeQuery[] rangeQueries = ingridQuery.getRangeQueries();
        for (RangeQuery rangeQuery : rangeQueries) {
        	// first try to add as NumericRangeQuery !
        	Query luceneQuery = null; 
        	try {
            	luceneQuery = NumericRangeQuery.newDoubleRange(rangeQuery.getRangeName(),
                	new Double(rangeQuery.getRangeFrom()), new Double(rangeQuery.getRangeTo()),
                	rangeQuery.isInclusive(), rangeQuery.isInclusive());        		
        	} catch (NumberFormatException ex) {
        		luceneQuery = null;
        	}
        	
        	// if problems add as TermRangeQuery
        	if (luceneQuery == null) {
        		// add TermRangeQuery
        		luceneQuery = new TermRangeQuery(rangeQuery.getRangeName(),
                    rangeQuery.getRangeFrom().toLowerCase(), rangeQuery.getRangeTo().toLowerCase(),
                    rangeQuery.isInclusive(), rangeQuery.isInclusive());
        	}

            booleanQuery.add(luceneQuery, transform(rangeQuery.isRequred(), rangeQuery.isProhibited()));
        }
    }
}
