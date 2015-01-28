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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.ingrid.utils.query.IngridQuery;

/**
 * Parse IngridQuery and add Query(s) for index field "content" to LuceneQuery.
 */
@Service
@Qualifier("contentParser")
public class ContentQueryParser extends AbstractParser {

    private static final String CONTENT = "content";

    private TermQueryParser termQueryParser = null;
    private WildCardTermQueryParser wildCardTermQueryParser = null;
    private FuzzyTermQueryParser fuzzyTermQueryParser = null;

    @Autowired
    public ContentQueryParser(Stemmer stemmer) {
    	this.termQueryParser = new TermQueryParser(CONTENT, null, stemmer);
    	this.wildCardTermQueryParser = new WildCardTermQueryParser(CONTENT, null);
    	this.fuzzyTermQueryParser = new FuzzyTermQueryParser(CONTENT, null);
    }

    public void parse(IngridQuery ingridQuery, BooleanQuery booleanQuery) {
    	termQueryParser.parse(ingridQuery, booleanQuery);
    	wildCardTermQueryParser.parse(ingridQuery, booleanQuery);
    	fuzzyTermQueryParser.parse(ingridQuery, booleanQuery);
    }
}
