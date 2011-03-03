package de.ingrid.admin.search;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.BooleanClause.Occur;

import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.TermQuery;

/**
 * Parse IngridQuery and add PrefixQuery(s) to LuceneQuery.
 */
public class PrefixQueryParser extends AbstractParser {

    private static Logger LOG = Logger.getLogger(PrefixQueryParser.class);

    private final String _field;
    private Occur _occur;

    public PrefixQueryParser(String field, Occur occur) {
        _field = field;
        _occur = occur;
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

            // create new query and add to boolean query
            if (value.indexOf(" ") == -1 && value.endsWith("*")) {
            	// remove "*"
                value = value.substring(0, value.length() - 1);

            	// add PrefixQuery
                Term term = new Term(_field, value);
                PrefixQuery prefixQuery = new PrefixQuery(term);

                // how to add new query to boolean query
                Occur occur = null;
                if (_occur != null) {
                    occur = _occur;
                } else {
                    occur = transform(ingridTermQuery.isRequred(), ingridTermQuery.isProhibited());
                }

                booleanQuery.add(prefixQuery, occur);            	
            }
        }
    }
}
