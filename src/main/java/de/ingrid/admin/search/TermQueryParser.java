package de.ingrid.admin.search;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause.Occur;

import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.TermQuery;

public abstract class TermQueryParser extends AbstractParser {

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
            if (value.indexOf(" ") == -1 && !value.endsWith("*")) {
                try {
                    value = _stemmer == null ? value : _stemmer.stem(value);
                } catch (IOException e) {
                    LOG.error("error while stemming: " + value, e);
                }
                Term term = new Term(_field, value);
                org.apache.lucene.search.TermQuery termQuery = new org.apache.lucene.search.TermQuery(term);
                Occur occur = null;
                if (_occur != null) {
                    occur = _occur;
                } else {
                    boolean required = ingridTermQuery.isRequred();
                    boolean prohibited = ingridTermQuery.isProhibited();
                    occur = transform(required, prohibited);
                }
                booleanQuery.add(termQuery, occur);
            }
        }

    }
}
