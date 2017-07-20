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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction.Modifier;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FieldValueFactorFunctionBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder.FilterFunctionBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.springframework.beans.factory.annotation.Autowired;

import de.ingrid.admin.Config;
import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.elasticsearch.IQueryParsers;
import de.ingrid.utils.query.ClauseQuery;
import de.ingrid.utils.query.IngridQuery;

public class QueryConverter implements IQueryParsers {
    
    private static Logger log = Logger.getLogger( QueryConverter.class );
    
    @Autowired
    private List<IQueryParsers> _queryConverter;

    public QueryConverter() {
        _queryConverter = new ArrayList<IQueryParsers>();
    }

    public void setQueryParsers(List<IQueryParsers> parsers) {
        this._queryConverter = parsers;
    }

    public BoolQueryBuilder convert(IngridQuery ingridQuery) {
        
        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        
        ClauseQuery[] clauses = ingridQuery.getClauses();
        for (ClauseQuery clauseQuery : clauses) {
            final BoolQueryBuilder res = convert(clauseQuery);
            if (clauseQuery.isRequred()) {
                if (clauseQuery.isProhibited())
                    qb.mustNot( res );
                else
                    qb.must( res );
            } else {
                qb.should( res );
            }
        }
        parse(ingridQuery, qb);
        
        return qb;
        
    }
    
    public void parse(IngridQuery ingridQuery, BoolQueryBuilder booleanQuery) {
        if (log.isDebugEnabled()) {
            log.debug("incoming ingrid query:" + ingridQuery.toString());
        }
        for (IQueryParsers queryConverter : _queryConverter) {
            if (log.isDebugEnabled()) {
                log.debug("incoming boolean query:" + booleanQuery.toString());
            }
            queryConverter.parse(ingridQuery, booleanQuery);
            if (log.isDebugEnabled()) {
                log.debug(queryConverter.toString() + ": resulting boolean query:" + booleanQuery.toString());
            }
        }
    }

    /**
     * Wrap a score modifier around the query, which uses a field from the document
     * to boost the score.
     * @param query is the query to apply the score modifier on
     * @return a new query which contains the score modifier and the given query
     */
    public QueryBuilder addScoreModifier(QueryBuilder query) {
        Config config = JettyStarter.getInstance().config;
        
        // describe the function to manipulate the score
        FieldValueFactorFunctionBuilder scoreFunc = ScoreFunctionBuilders
            .fieldValueFactorFunction( config.esBoostField )
            .modifier( getModifier(config.esBoostModifier) )
            .factor( config.esBoostFactor );
        
        // create the wrapper query to apply the score function to the query
        FilterFunctionBuilder[] functions = new FilterFunctionBuilder[1];
        functions[0] = new FunctionScoreQueryBuilder.FilterFunctionBuilder( query, scoreFunc );
        
        FunctionScoreQueryBuilder funcScoreQuery = new FunctionScoreQueryBuilder( functions );
        funcScoreQuery.boostMode( getBoostMode(config.esBoostMode) );
        return funcScoreQuery;
    }

    private Modifier getModifier(String esBoostModifier) {
        Modifier result = null;
        switch (esBoostModifier) {
        case "LN":
            result = Modifier.LN;
            break;
        case "LN1P":
            result = Modifier.LN1P;
            break;
        case "LN2P":
            result = Modifier.LN2P;
            break;
        case "LOG":
            result = Modifier.LOG;
            break;
        case "LOG1P":
            result = Modifier.LOG1P;
            break;
        case "LOG2P":
            result = Modifier.LOG2P;
            break;
        case "NONE":
            result = Modifier.NONE;
            break;
        case "RECIPROCAL":
            result = Modifier.RECIPROCAL;
            break;
        case "SQRT":
            result = Modifier.SQRT;
            break;
        case "SQUARE":
            result = Modifier.SQUARE;
            break;

        default:
            result = Modifier.LOG1P;
            break;
        }
        return result;
    }

    private CombineFunction getBoostMode(String esBoostMode) {
        CombineFunction result = null;
        switch (esBoostMode) {
        case "SUM":
            result = CombineFunction.SUM;
            break;
        case "AVG":
            result = CombineFunction.AVG;
            break;
        case "MAX":
            result = CombineFunction.MAX;
            break;
        case "MIN":
            result = CombineFunction.MIN;
            break;
        case "MULTIPLY":
            result = CombineFunction.MULTIPLY;
            break;
        case "REPLACE":
            result = CombineFunction.REPLACE;
            break;
        default:
            result = CombineFunction.SUM;
            break;
        }
        return result;
    }

}
