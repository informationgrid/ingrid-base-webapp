package de.ingrid.admin.query;

import org.apache.lucene.search.BooleanQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.ingrid.utils.query.IngridQuery;

@Service
@Qualifier("queryParser")
public class QueryParser implements IQueryParser {

    private final IQueryParser _defaultParser;

    @Autowired
    public QueryParser(@Qualifier("defaultParser") IQueryParser defaultParser) {
        _defaultParser = defaultParser;
    }

    public void parse(IngridQuery ingridQuery, BooleanQuery booleanQuery) {
        _defaultParser.parse(ingridQuery, booleanQuery);
    }

}
