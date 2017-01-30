/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
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
package de.ingrid.admin.elasticsearch.converter;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import de.ingrid.admin.elasticsearch.IQueryParsers;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.TermQuery;

public class GlobalQueryConverter implements IQueryParsers {

    @Override
    public void parse(IngridQuery ingridQuery, BoolQueryBuilder queryBuilder) {
        TermQuery[] terms = ingridQuery.getTerms();

        BoolQueryBuilder bq = QueryBuilders.boolQuery();
        
        if (terms.length == 0) {
            bq.must( QueryBuilders.matchAllQuery() );
            queryBuilder.must( bq );
        } else {
            for (TermQuery term : terms) {
                if (term.isRequred()) {
                    bq.must( QueryBuilders.queryStringQuery( term.getTerm() ) );
                    
                } else {
                    bq.should( QueryBuilders.queryStringQuery( term.getTerm() ) );
                    
                }
            }
            
            if (terms[0].isRequred()) {
                queryBuilder.must( bq );
            } else {
                queryBuilder.should( bq );
            }
        }
    }
}
