/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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
import org.springframework.stereotype.Service;

import de.ingrid.admin.elasticsearch.IQueryParsers;
import de.ingrid.utils.query.IngridQuery;

@Service
public class MatchAllQueryConverter implements IQueryParsers {
    
    @Override
    public void parse(IngridQuery ingridQuery, BoolQueryBuilder queryBuilder) {
        // NOTICE: is also called on sub clauses BUT WE ONLY PROCESS THE TOP INGRID QUERY.
        // all other ones are subclasses !
        //
        boolean isTopQuery = (ingridQuery.getClass().equals(IngridQuery.class));
        boolean hasTerms = ingridQuery.getTerms().length > 0;
        if (!hasTerms && isTopQuery && !queryBuilder.hasClauses()) {
            BoolQueryBuilder bq = QueryBuilders.boolQuery();
            bq.must( QueryBuilders.matchAllQuery() );
            queryBuilder.must( bq );
        }
    }

}
