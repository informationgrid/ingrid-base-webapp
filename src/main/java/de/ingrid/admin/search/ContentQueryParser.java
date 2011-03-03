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
    private PhraseQueryParser phraseQueryParser = null;
    private PrefixQueryParser prefixQueryParser = null;

    @Autowired
    public ContentQueryParser(Stemmer stemmer) {
    	this.termQueryParser = new TermQueryParser("content", null, stemmer);
    	this.phraseQueryParser = new PhraseQueryParser("content", null);
    	this.prefixQueryParser = new PrefixQueryParser("content", null);
    }


    public void parse(IngridQuery ingridQuery, BooleanQuery booleanQuery) {
    	termQueryParser.parse(ingridQuery, booleanQuery);
    	phraseQueryParser.parse(ingridQuery, booleanQuery);
    	prefixQueryParser.parse(ingridQuery, booleanQuery);
    }
}
