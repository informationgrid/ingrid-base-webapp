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
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Service;

import de.ingrid.admin.elasticsearch.IQueryParsers;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.WildCardFieldQuery;

@Service
public class WildcardFieldQueryConverter implements IQueryParsers {
    
    @Override
    public void parse(IngridQuery ingridQuery, BoolQueryBuilder queryBuilder) {
        WildCardFieldQuery[] wildFields = ingridQuery.getWildCardFieldQueries();

        BoolQueryBuilder bq = null;
        boolean wasAndConnection = false;
        
        for (WildCardFieldQuery fieldQuery : wildFields) {
            
            //QueryBuilder subQuery = QueryBuilders.matchQuery( fieldQuery.getFieldName(), fieldQuery.getFieldValue() );
            QueryBuilder subQuery = QueryBuilders.wildcardQuery( fieldQuery.getFieldName(), fieldQuery.getFieldValue() );
            
            if (fieldQuery.isRequred()) {
                if (bq == null) bq = QueryBuilders.boolQuery();
                if (fieldQuery.isProhibited()) {
                    bq.mustNot( subQuery );
                } else {                        
                    bq.must( subQuery );
                }
                wasAndConnection = true;
                
            } else {
                // if it's an OR-connection then the currently built query must become a sub-query
                // so that the AND/OR connection is correctly transformed. In case there was an
                // AND-connection before, the transformation would become:
                // OR( (term1 AND term2), term3)
                if (bq == null) bq = QueryBuilders.boolQuery();
                
                if (!wasAndConnection) {
                    bq.should( subQuery );
                    
                } else {
                    BoolQueryBuilder parentBq = QueryBuilders.boolQuery();
                    parentBq.should( bq ).should( subQuery );
                    bq = parentBq;
                    wasAndConnection = false;
                }
                
            }
        }
        if (bq != null) {
            if (wildFields[0].isRequred()) {
                queryBuilder.must( bq );
            } else {
                queryBuilder.should( bq );
            }
        }
    }

}
