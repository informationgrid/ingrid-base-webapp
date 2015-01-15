/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.BooleanClause.Occur;

import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.WildCardTermQuery;

/**
 * Maps WildCardTermQuery(s) from IngridQuery to LuceneQuery.
 */
public class WildCardTermQueryParser extends AbstractParser {

//    private static Logger LOG = Logger.getLogger(WildCardTermQueryParser.class);

    private final String _field;
    private Occur _occur;

    public WildCardTermQueryParser(String field, Occur occur) {
        _field = field;
        _occur = occur;
    }

    public void parse(IngridQuery ingridQuery, BooleanQuery booleanQuery) {
        WildCardTermQuery[] wildCardTermQueries = ingridQuery.getWildCardTermQueries();
        for (WildCardTermQuery wildCardTermQuery : wildCardTermQueries) {

        	// how to add new query to boolean query
            Occur occur = null;
            if (_occur != null) {
                occur = _occur;
            } else {
                occur = transform(wildCardTermQuery.isRequred(), wildCardTermQuery.isProhibited());
            }

            WildcardQuery luceneQuery = new WildcardQuery(new Term(_field,
            		wildCardTermQuery.getTerm().toLowerCase()));

            booleanQuery.add(luceneQuery, occur);
        }
    }
}
