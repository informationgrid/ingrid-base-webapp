package de.ingrid.admin.search;

import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.BooleanClause.Occur;

import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.TermQuery;

/**
 * Parse IngridQuery and add PhraseQuery(s) to LuceneQuery.
 */
public class PhraseQueryParser extends AbstractParser {

    private static Logger LOG = Logger.getLogger(PhraseQueryParser.class);

    private final String _field;
    private Occur _occur;

    public PhraseQueryParser(String field, Occur occur) {
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
            if (value.indexOf(" ") > -1) {
            	// add PhraseQuery
                PhraseQuery phraseQuery = new PhraseQuery();
                StringTokenizer tokenizer = new StringTokenizer(value);
                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken();
                    final String filteredTerm = filterTerm(token);
                    phraseQuery.add(new Term(_field, filteredTerm));
                }
                if (phraseQuery.getTerms().length > 0) {
                    // how to add new query to boolean query
                    Occur occur = null;
                    if (_occur != null) {
                        occur = _occur;
                    } else {
                        occur = transform(ingridTermQuery.isRequred(), ingridTermQuery.isProhibited());
                    }

                    booleanQuery.add(phraseQuery, occur);
                }
            }
        }
    }
}
