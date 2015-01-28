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

import java.util.StringTokenizer;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TermQuery;
import org.springframework.stereotype.Service;

import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;

/**
 * Generic mapping of FieldQuery(s) from IngridQuery to LuceneQuery.
 */
@Service
public class FieldQueryParser extends AbstractParser {

    @Override
    public void parse(IngridQuery ingridQuery, BooleanQuery booleanQuery) {
        FieldQuery[] fields = ingridQuery.getFields();
        for (FieldQuery fieldQuery : fields) {
            // filter term with default analyzer set in parent !
            final String term = filterTerm(fieldQuery.getFieldValue());
            final String field = fieldQuery.getFieldName();

            Occur occur = transform(fieldQuery.isRequred(), fieldQuery.isProhibited());
            // check if it is no phrase query
            if (term.indexOf(' ') == -1) {
                Term luceneTerm = new Term(field, term);
                TermQuery luceneTermQuery = new TermQuery(luceneTerm);
                booleanQuery.add(luceneTermQuery, occur);
            } else {
                PhraseQuery phraseQuery = new PhraseQuery();
                StringTokenizer tokenizer = new StringTokenizer(term);
                while (tokenizer.hasMoreTokens()) {
                    phraseQuery.add(new Term(field, tokenizer.nextToken()));
                }
                booleanQuery.add(phraseQuery, occur);
            }
        }
    }
}
