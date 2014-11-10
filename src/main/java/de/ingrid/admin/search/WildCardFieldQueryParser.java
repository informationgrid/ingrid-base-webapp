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
import org.springframework.stereotype.Service;

import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.WildCardFieldQuery;

/**
 * Generic mapping of WildCardFieldQuery(s) from IngridQuery to LuceneQuery.
 */
@Service
public class WildCardFieldQueryParser extends AbstractParser {

    @Override
    public void parse(IngridQuery ingridQuery, BooleanQuery booleanQuery) {
        WildCardFieldQuery[] wildCardFieldQueries = ingridQuery.getWildCardFieldQueries();
        for (WildCardFieldQuery wildCardQuery : wildCardFieldQueries) {
        	WildcardQuery luceneQuery =
        		new WildcardQuery(new Term(wildCardQuery.getFieldName(), wildCardQuery.getFieldValue()));

            booleanQuery.add(luceneQuery, transform(wildCardQuery.isRequred(), wildCardQuery.isProhibited()));
        }
    }
}
