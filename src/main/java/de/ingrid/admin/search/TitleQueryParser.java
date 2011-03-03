package de.ingrid.admin.search;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.ingrid.utils.query.IngridQuery;

/**
 * Parse IngridQuery and add Query(s) for index field "title" to LuceneQuery.
 */
@Service
@Qualifier("titleParser")
public class TitleQueryParser implements IQueryParser {

    private TermQueryParser termQueryParser = null;
    private PhraseQueryParser phraseQueryParser = null;
    private PrefixQueryParser prefixQueryParser = null;

    public TitleQueryParser() {
    	this.termQueryParser = new TermQueryParser("title", Occur.SHOULD, null);
    	this.phraseQueryParser = new PhraseQueryParser("title", Occur.SHOULD);
    	this.prefixQueryParser = new PrefixQueryParser("title", Occur.SHOULD);
    }

    public void parse(IngridQuery ingridQuery, BooleanQuery booleanQuery) {
    	termQueryParser.parse(ingridQuery, booleanQuery);
    	phraseQueryParser.parse(ingridQuery, booleanQuery);
    	prefixQueryParser.parse(ingridQuery, booleanQuery);
    }
}
