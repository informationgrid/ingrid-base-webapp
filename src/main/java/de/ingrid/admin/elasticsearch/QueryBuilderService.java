package de.ingrid.admin.elasticsearch;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TypeQueryBuilder;
import org.springframework.stereotype.Service;

@Service
public class QueryBuilderService {

    private static Logger log = LogManager.getLogger( QueryBuilderService.class );

    public BoolQueryBuilder createIndexTypeFilter(String[] activeIndices) {

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        BoolQueryBuilder boolShould = QueryBuilders.boolQuery();
        List<QueryBuilder> should = boolShould.should();

        for (String indexTypes : activeIndices) {
            String[] indexSplitType = indexTypes.split( ":" );
            should.add( buildIndexTypeMust( indexSplitType[0], indexSplitType[1] ) );
        }

        boolQuery.filter().add( boolShould );

        return boolQuery;
    }

    public BoolQueryBuilder buildMustQuery(String... fieldAndValue) {

        if (fieldAndValue.length % 2 == 1) {
            log.error( "This function only should have an even number of parameters!" );
            throw new RuntimeException( "ERROR: uneven number of parameters" );
        }

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        List<QueryBuilder> must = boolQuery.must();
        for (int i = 0; i < fieldAndValue.length; i++) {
            must.add( QueryBuilders.termQuery( fieldAndValue[i], fieldAndValue[i + 1] ) );
            i++;
        }

        return boolQuery;
    }

    public BoolQueryBuilder buildIndexTypeMust(String index, String type) {
        TermQueryBuilder indexQuery = QueryBuilders.termQuery( "_index", index );
        TypeQueryBuilder typeQuery = QueryBuilders.typeQuery( type );

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        List<QueryBuilder> must = boolQuery.must();
        must.add( indexQuery );
        must.add( typeQuery );

        return boolQuery;
    }

    public BoolQueryBuilder createQueryWithFilter(String query, BoolQueryBuilder indexTypeFilter) {
        QueryStringQueryBuilder queryStringQuery = QueryBuilders.queryStringQuery( query.trim().length() == 0 ? "*" : query );
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        List<QueryBuilder> must = boolQuery.must();
        must.add( queryStringQuery );
        must.add( QueryBuilders.boolQuery().should(indexTypeFilter) );

        return boolQuery;
    }
}
