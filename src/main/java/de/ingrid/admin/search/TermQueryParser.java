package de.ingrid.admin.search;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause.Occur;

import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.TermQuery;

public abstract class TermQueryParser implements IQueryParser {

    private final String _field;
    private Occur _occur;

    public TermQueryParser(String field, Occur occur) {
        _field = field;
        _occur = occur;
    }

    public TermQueryParser(String field) {
        _field = field;
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
                Term term = new Term(_field, value);
                org.apache.lucene.search.TermQuery termQuery = new org.apache.lucene.search.TermQuery(term);
                Occur occur = null;
                if (_occur != null) {
                    occur = _occur;
                } else {
                    boolean required = ingridTermQuery.isRequred();
                    boolean prohibited = ingridTermQuery.isProhibited();
                    if (required) {
                        if (prohibited) {
                            occur = Occur.MUST_NOT;
                        } else {
                            occur = Occur.MUST;
                        }
                    } else {
                        if (prohibited) {
                            occur = Occur.MUST_NOT;
                        } else {
                            occur = Occur.SHOULD;
                        }
                    }
                }
                booleanQuery.add(termQuery, occur);
            }
        }

    }

}
