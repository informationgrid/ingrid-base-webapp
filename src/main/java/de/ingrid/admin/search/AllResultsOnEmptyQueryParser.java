package de.ingrid.admin.search;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.springframework.stereotype.Service;

import de.ingrid.utils.query.IngridQuery;

@Service
public class AllResultsOnEmptyQueryParser extends AbstractParser {

    @Override
    public void parse(final IngridQuery ingridQuery, final BooleanQuery booleanQuery) {
    	
    	// NOTICE: is also called on sub clauses BUT WE ONLY PROCESS THE TOP INGRID QUERY.
    	// all other ones are subclasses !
    	boolean isTopQuery = (ingridQuery.getClass().equals(IngridQuery.class));
        if (isTopQuery && booleanQuery.getClauses().length == 0) {
        	// all processed and "empty" Lucene query
        	booleanQuery.add(new MatchAllDocsQuery(), transform(false, false));
        }
    }

}
