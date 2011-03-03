package de.ingrid.admin.search;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.BooleanClause.Occur;

import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.TermQuery;

/**
 * Maps TermQuery(s) from IngridQuery to LuceneQuery.
 */
public class TermQueryParser extends AbstractParser {

    private static Logger LOG = Logger.getLogger(TermQueryParser.class);

    private final String _field;
    private Occur _occur;
    private final Stemmer _stemmer;

    public TermQueryParser(String field, Occur occur, Stemmer stemmer) {
        _field = field;
        _occur = occur;
        _stemmer = stemmer;
    }

    public void parse(IngridQuery ingridQuery, BooleanQuery booleanQuery) {
        TermQuery[] ingridTerms = ingridQuery.getTerms();
        for (TermQuery ingridTermQuery : ingridTerms) {
            if (ingridTermQuery == null) {
                continue;
            }
            String value = ingridTermQuery.getTerm();
            if (value == null) {
                continue;
            }
            value = value.toLowerCase();

            // how to add new query to boolean query
            Occur occur = null;
            if (_occur != null) {
                occur = _occur;
            } else {
                occur = transform(ingridTermQuery.isRequred(), ingridTermQuery.isProhibited());
            }

            // create new query and add to boolean query
            if (value.indexOf(" ") > -1) {
            	// add PhraseQuery
            	addPhraseQuery(booleanQuery, value, occur);

            } else {
                if (value.endsWith("*")) {
                	addPrefixQuery(booleanQuery, value, occur);

                } else {
                	addTermQuery(booleanQuery, value, occur);
                }
            }
        }
    }

    private void addPhraseQuery(BooleanQuery booleanQuery, String value, Occur occur) {
    	// add PhraseQuery
        PhraseQuery phraseQuery = new PhraseQuery();
        StringTokenizer tokenizer = new StringTokenizer(value);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            // Filtering with StandardAnalyzer e.g. to remove "*" at end !
            final String filteredTerm = filterTerm(token);
            phraseQuery.add(new Term(_field, filteredTerm));
        }
        if (phraseQuery.getTerms().length > 0) {
            booleanQuery.add(phraseQuery, occur);
        }
    }

    private void addPrefixQuery(BooleanQuery booleanQuery, String value, Occur occur) {
        if (value.endsWith("*")) {
        	// remove "*"
            value = value.substring(0, value.length() - 1);
        }

    	// add PrefixQuery
        Term term = new Term(_field, value);
        PrefixQuery prefixQuery = new PrefixQuery(term);
        booleanQuery.add(prefixQuery, occur);            	
    }

    private void addTermQuery(BooleanQuery booleanQuery, String value, Occur occur) {
/*
    	// filter and use phrase like in former AbstractSearcher ? NO ...
        String filteredTerm = filterTerm(value);
        if (filteredTerm.indexOf(" ") > -1) {
            addPhraseQuery(booleanQuery, filteredTerm, occur);
            return;
        }
*/
        if (_stemmer != null) {
            try {
                value = _stemmer.stem(value);
            } catch (IOException e) {
                LOG.error("error while stemming: " + value, e);
            }            		
    	}

        Term term = new Term(_field, value);
        org.apache.lucene.search.TermQuery termQuery = new org.apache.lucene.search.TermQuery(term);
        booleanQuery.add(termQuery, occur);
    }
}
