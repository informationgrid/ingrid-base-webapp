package de.ingrid.admin.search;

import org.apache.lucene.search.BooleanQuery;

import de.ingrid.utils.query.IngridQuery;

public interface IQueryParser {

    void parse(IngridQuery ingridQuery, BooleanQuery booleanQuery);
}
