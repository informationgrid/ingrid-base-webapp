package de.ingrid.admin.search;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.ingrid.search.utils.IQueryParser;
import de.ingrid.utils.query.IngridQuery;

/**
 * Parse IngridQuery and add Query(s) for index field "title" to LuceneQuery.
 */
@Service
@Qualifier("titleParser")
public class TitleQueryParser implements IQueryParser {

    private TermQueryParser termQueryParser = null;

    @Autowired
    public TitleQueryParser(Stemmer stemmer) {
    	this.termQueryParser = new TermQueryParser("title", Occur.SHOULD, stemmer);
    }

    public void parse(IngridQuery ingridQuery, BooleanQuery booleanQuery) {
    	termQueryParser.parse(ingridQuery, booleanQuery);
    }
}
