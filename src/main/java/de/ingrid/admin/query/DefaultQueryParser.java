package de.ingrid.admin.query;

import org.apache.lucene.search.BooleanQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.ingrid.utils.query.IngridQuery;

@Service
@Qualifier("defaultParser")
public class DefaultQueryParser implements IQueryParser {

    private final IQueryParser _titleParser;
    private final IQueryParser _contentParser;

    @Autowired
    public DefaultQueryParser(@Qualifier("titleParser") IQueryParser titleParser,
            @Qualifier("contentParser") IQueryParser contentParser) {
        _titleParser = titleParser;
        _contentParser = contentParser;
    }

    public void parse(IngridQuery ingridQuery, BooleanQuery booleanQuery) {
        _titleParser.parse(ingridQuery, booleanQuery);
        _contentParser.parse(ingridQuery, booleanQuery);
    }

}
