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
import org.apache.lucene.search.MatchAllDocsQuery;
import org.springframework.stereotype.Service;

import de.ingrid.utils.query.IngridQuery;

@Service
public class AllResultsOnEmptyQueryParser extends AbstractParser {

    @Override
    public void parse(final IngridQuery ingridQuery, final BooleanQuery booleanQuery) {
    	
    	// NOTICE: is also called on sub clauses BUT WE ONLY PROCESS THE TOP INGRID QUERY.
    	// all other ones are subclasses !
    	boolean isTopQuery = (ingridQuery.getClass().equals(IngridQuery.class));
        if (isTopQuery && booleanQuery.getClauses().length == 0) {
        	// all processed and "empty" Lucene query
        	booleanQuery.add(new MatchAllDocsQuery(), transform(false, false));
        }
    }

}
