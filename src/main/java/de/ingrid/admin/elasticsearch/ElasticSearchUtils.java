/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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
package de.ingrid.admin.elasticsearch;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction.Modifier;

public class ElasticSearchUtils {
    
    private static Logger log = Logger.getLogger( ElasticSearchUtils.class ); 

    /**
     * Generate a new ID for a new index of the format <index-name>_<id>, where <id> is number counting up.
     * 
     * @param name
     * @return
     */
    public static String getNextIndexName(String name) {
        if (name == null) {
            throw new RuntimeException( "Old index name must not be null!" );
        }
        boolean isNew = false;

        SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyyMMddHHmmssS" );

        int delimiterPos = name.lastIndexOf( "_" );
        if (delimiterPos == -1) {
            isNew = true;
        } else {
            try {
                dateFormat.parse( name.substring( delimiterPos + 1 ) );
            } catch (Exception ex) {
                isNew = true;
            }
        }

        String date = dateFormat.format( new Date() );

        if (isNew) {
            return name + "_" + date;
        } else {
            return name.substring( 0, delimiterPos + 1 ) + date;
        }
    }

    public static SearchType getSearchTypeFromString( String input ) {
        SearchType type;
        switch (input) {
        case "COUNT":
            type = SearchType.COUNT;
            break;
        case "DEFAULT":
            type = SearchType.DEFAULT;
            break;
        case "DFS_QUERY_AND_FETCH":
            type = SearchType.DFS_QUERY_AND_FETCH;
            break;
        case "DFS_QUERY_THEN_FETCH":
            type = SearchType.DFS_QUERY_THEN_FETCH;
            break;
        case "QUERY_AND_FETCH":
            type = SearchType.QUERY_AND_FETCH;
            break;
        case "QUERY_THEN_FETCH":
            type = SearchType.QUERY_THEN_FETCH;
            break;
        case "SCAN":
            type = SearchType.SCAN;
            break;
        default:
            log.error( "Unknown SearchType (" + input + "), using default one: DFS_QUERY_THEN_FETCH" );
            type = SearchType.DFS_QUERY_THEN_FETCH;
        }
        return type;
    }
    
    public static Modifier getModifierFromString( String input ) {
        Modifier modifier = null;
        switch (input) {
        case "none":
            modifier = Modifier.NONE;
            break;
        case "log":
            modifier = Modifier.LOG;
            break;
        case "log1p":
            modifier = Modifier.LOG1P;
            break;
        case "log2p":
            modifier = Modifier.LOG2P;
            break;
        case "ln":
            modifier = Modifier.LN;
            break;
        case "ln1p":
            modifier = Modifier.LN1P;
            break;
        case "ln2p":
            modifier = Modifier.LN2P;
            break;
        case "square":
            modifier = Modifier.SQUARE;
            break;
        case "sqrt":
            modifier = Modifier.SQRT;
            break;
        case "reciprocal":
            modifier = Modifier.RECIPROCAL;
            break;
        }
        return modifier;
    }
}
