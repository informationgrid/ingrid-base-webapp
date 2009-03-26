package de.ingrid.admin.query;

import org.apache.lucene.search.BooleanQuery;

import de.ingrid.utils.query.IngridQuery;

public interface IQueryParser {

    void parse(IngridQuery ingridQuery, BooleanQuery booleanQuery);
}
