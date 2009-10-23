package de.ingrid.admin.search;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanClause.Occur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.utils.query.ClauseQuery;
import de.ingrid.utils.query.IngridQuery;

@Service
public class QueryParsers extends AbstractParser {

    private final IQueryParser[] _queryParsers;

    @Autowired
    public QueryParsers(IQueryParser... queryParsers) {
        _queryParsers = queryParsers;
    }

    public Query parse(IngridQuery ingridQuery) {
        BooleanQuery booleanQuery = new BooleanQuery();
        ClauseQuery[] clauses = ingridQuery.getClauses();
        for (ClauseQuery clauseQuery : clauses) {
            final Query sc = parse(clauseQuery);
            if (!sc.equals(new BooleanQuery())) {
                Occur occur = transform(clauseQuery.isRequred(), clauseQuery.isProhibited());
                booleanQuery.add(sc, occur);
            }
        }
        parse(ingridQuery, booleanQuery);
        return booleanQuery;
    }

    @Override
    public void parse(IngridQuery ingridQuery, BooleanQuery booleanQuery) {
        for (IQueryParser queryParser : _queryParsers) {
            queryParser.parse(ingridQuery, booleanQuery);
        }
    }

}
