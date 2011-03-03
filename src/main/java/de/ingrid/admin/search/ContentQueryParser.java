package de.ingrid.admin.search;

import org.apache.lucene.search.BooleanQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.ingrid.utils.query.IngridQuery;

/**
 * Parse IngridQuery and add Query(s) for index field "content" to LuceneQuery.
 */
@Service
@Qualifier("contentParser")
public class ContentQueryParser implements IQueryParser {

    private TermQueryParser termQueryParser = null;

    @Autowired
    public ContentQueryParser(Stemmer stemmer) {
//    	this.termQueryParser = new TermQueryParser("content", null, stemmer);
    	// do NOT use stemmer ! language specific !
    	this.termQueryParser = new TermQueryParser("content", null, null);
    }


    public void parse(IngridQuery ingridQuery, BooleanQuery booleanQuery) {
    	termQueryParser.parse(ingridQuery, booleanQuery);
    }
}
