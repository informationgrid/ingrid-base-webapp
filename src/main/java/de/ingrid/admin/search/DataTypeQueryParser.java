/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
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
