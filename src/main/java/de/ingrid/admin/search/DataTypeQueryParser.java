package de.ingrid.admin.search;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.springframework.stereotype.Service;

import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;

@Service
public class DataTypeQueryParser extends AbstractParser {

    @Override
    public void parse(final IngridQuery ingridQuery, final BooleanQuery booleanQuery) {
        final FieldQuery[] dataTypes = ingridQuery.getDataTypes();
        if (dataTypes != null) {
            for (final FieldQuery dataType : dataTypes) {
                final String field = dataType.getFieldName();
                final String value = dataType.getFieldValue().toLowerCase();
                final Occur occur = transform(dataType.isRequred(), dataType.isProhibited());
                final Term term = new Term(field, value);
                booleanQuery.add(new TermQuery(term), occur);
            }
        }
    }

}
