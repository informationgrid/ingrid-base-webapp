package de.ingrid.admin.search;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.springframework.stereotype.Service;

import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;

@Service
public class FieldQueryParser extends AbstractParser {

    @Override
    public void parse(IngridQuery ingridQuery, BooleanQuery booleanQuery) {
        FieldQuery[] fields = ingridQuery.getFields();
        for (FieldQuery fieldQuery : fields) {
            final String term = fieldQuery.getFieldValue().toLowerCase();
            final String field = fieldQuery.getFieldName();

            // check if it is no phrase query
            if (term.indexOf(' ') == -1) {
                Occur occur = transform(fieldQuery.isRequred(), fieldQuery.isProhibited());
                Term luceneTerm = new Term(field, term);
                TermQuery luceneTermQuery = new TermQuery(luceneTerm);
                booleanQuery.add(luceneTermQuery, occur);
            }
        }

    }

}
