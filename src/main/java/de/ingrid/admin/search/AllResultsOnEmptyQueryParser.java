package de.ingrid.admin.search;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.springframework.stereotype.Service;

import de.ingrid.utils.query.IngridQuery;

@Service
public class AllResultsOnEmptyQueryParser extends AbstractParser {

    @Override
    public void parse(final IngridQuery ingridQuery, final BooleanQuery booleanQuery) {
    	
        if (booleanQuery.getClauses().length == 0) {
        	booleanQuery.add(new MatchAllDocsQuery(), transform(false, false));
        }
    }

}
