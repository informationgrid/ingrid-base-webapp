/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.admin.elasticsearch;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.ParseException;
import de.ingrid.utils.queryparser.QueryStringParser;

public class Utils {
    public static final long MAX_RESULTS = 11;
    
    public static IngridQuery getIngridQuery( String term ) {
        try {
            return QueryStringParser.parse( term );
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static void checkHitsForIDs(IngridHit[] hits, int... ids) {
        for (int id : ids) {
            boolean found = false;
            for (IngridHit hit : hits) {
                if (Integer.valueOf( hit.getDocumentId() ) == id) {
                    found = true;
                    break;
                }
            }
            assertThat("The following ID was not found in the results: " + id, found, is(true));
        }        
    }
    
    public static void addDefaultFacets(IngridQuery ingridQuery) {
        IngridDocument f1 = new IngridDocument();
        f1.put("id", "partner");

        IngridDocument f2 = new IngridDocument();
        f2.put("id", "after");
        Map<String, String> classes = new HashMap<String, String>();
        classes.put("id", "April2014");
        classes.put("query", "t1:2014-05-01 t2:2014-09-01");
        f2.put("classes", Arrays.asList(new Object[] { classes }));

        IngridDocument f3 = new IngridDocument();
        f3.put("id", "datatype");
        Map<String, String> classes2 = new HashMap<String, String>();
        classes2.put("id", "bundPDFs");
        classes2.put("query", "partner:bund datatype:pdf");
        f3.put("classes", Arrays.asList(new Object[] { classes2 }));

        ingridQuery.put("FACETS", Arrays.asList(new Object[] { f1, f2, f3 }));
    }
}
