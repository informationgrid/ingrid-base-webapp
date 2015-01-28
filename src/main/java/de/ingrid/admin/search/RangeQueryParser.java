/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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
package de.ingrid.admin.search;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.springframework.stereotype.Service;

import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.RangeQuery;

/**
 * Generic mapping of RangeQuery(s) from IngridQuery to LuceneQuery.
 */
@Service
public class RangeQueryParser extends AbstractParser {

    @Override
    public void parse(IngridQuery ingridQuery, BooleanQuery booleanQuery) {
        RangeQuery[] rangeQueries = ingridQuery.getRangeQueries();
        for (RangeQuery rangeQuery : rangeQueries) {
        	// first try to add as NumericRangeQuery !
        	Query luceneQuery = null; 
        	try {
            	luceneQuery = NumericRangeQuery.newDoubleRange(rangeQuery.getRangeName(),
                	new Double(rangeQuery.getRangeFrom()), new Double(rangeQuery.getRangeTo()),
                	rangeQuery.isInclusive(), rangeQuery.isInclusive());        		
        	} catch (NumberFormatException ex) {
        		luceneQuery = null;
        	}
        	
        	// if problems add as TermRangeQuery
        	if (luceneQuery == null) {
        		// add TermRangeQuery
        		luceneQuery = new TermRangeQuery(rangeQuery.getRangeName(),
                    rangeQuery.getRangeFrom().toLowerCase(), rangeQuery.getRangeTo().toLowerCase(),
                    rangeQuery.isInclusive(), rangeQuery.isInclusive());
        	}

            booleanQuery.add(luceneQuery, transform(rangeQuery.isRequred(), rangeQuery.isProhibited()));
        }
    }
}
