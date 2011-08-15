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
public class ContentQueryParser extends AbstractParser {

    private static final String CONTENT = "content";

    private TermQueryParser termQueryParser = null;
    private WildCardTermQueryParser wildCardTermQueryParser = null;
    private FuzzyTermQueryParser fuzzyTermQueryParser = null;

    @Autowired
    public ContentQueryParser(Stemmer stemmer) {
    	this.termQueryParser = new TermQueryParser(CONTENT, null, stemmer);
    	this.wildCardTermQueryParser = new WildCardTermQueryParser(CONTENT, null);
    	this.fuzzyTermQueryParser = new FuzzyTermQueryParser(CONTENT, null);
    }

    public void parse(IngridQuery ingridQuery, BooleanQuery booleanQuery) {
    	termQueryParser.parse(ingridQuery, booleanQuery);
    	wildCardTermQueryParser.parse(ingridQuery, booleanQuery);
    	fuzzyTermQueryParser.parse(ingridQuery, booleanQuery);
    }
}
