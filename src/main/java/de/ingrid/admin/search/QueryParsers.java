package de.ingrid.admin.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanClause.Occur;
import org.springframework.stereotype.Service;

import de.ingrid.utils.query.ClauseQuery;
import de.ingrid.utils.query.IngridQuery;

@Service
public class QueryParsers extends AbstractParser {

	// Injected by Spring (XML based !)
    private List<IQueryParser> _queryParsers;

    // NO autowiring here ! We define instance of this class via classical XML to be 
    // able to set the types and order of the parsers !!!
//    @Autowired
/*
    public QueryParsers(IQueryParser... queryParsers) {
        _queryParsers = Arrays.asList(queryParsers);
    }
*/
    public QueryParsers() {
        _queryParsers = new ArrayList<IQueryParser>();
    }

	public void setQueryParsers(List<IQueryParser> parsers) {
		this._queryParsers = parsers;
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
