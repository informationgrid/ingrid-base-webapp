/*
 * **************************************************-
 * ingrid-iplug-se-iplug
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
package de.ingrid.admin.elasticsearch;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;

import de.ingrid.admin.JettyStarter;

public class ElasticSearchUtils {

    public static boolean typeExists(String type, Client client) {
        TypesExistsRequest typeRequest = new TypesExistsRequest( new String[] { JettyStarter.getInstance().config.index }, type );
        boolean typeExists = client.admin().indices().typesExists( typeRequest ).actionGet().isExists();
        return typeExists;
    }

    /**
     * Create a new index if it doesn't already exists.
     * @param client
     * @param name
     * @return true, if a new index was created otherwise false if it already existed
     */
    public static boolean createIndex(Client client, String name) {
        boolean indexExists = client.admin().indices().prepareExists( name ).execute().actionGet().isExists();
        if (!indexExists) {
            client.admin().indices().prepareCreate( name ).execute().actionGet();
            return true;
        }
        return false;
    }

    public static String getIndexNameFromAliasName(Client client) {
        ImmutableOpenMap<String, AliasMetaData> indexToAliasesMap = client.admin().cluster().state( Requests.clusterStateRequest() ).actionGet().getState().getMetaData().aliases()
                .get( JettyStarter.getInstance().config.index );
        if (indexToAliasesMap != null && !indexToAliasesMap.isEmpty()) {
            return indexToAliasesMap.keys().iterator().next().value;
        }
        return null;
    }

    public static void switchAlias(Client client, String oldIndex, String newIndex) {
        String aliasName = JettyStarter.getInstance().config.index;
        IndicesAliasesRequestBuilder prepareAliases = client.admin().indices().prepareAliases();
        if (oldIndex != null) {
            prepareAliases.removeAlias( oldIndex, aliasName );
        }
        prepareAliases.addAlias( newIndex, aliasName ).execute().actionGet();
    }
    
    public static void removeAlias(Client client, String index) {
        String aliasName = JettyStarter.getInstance().config.index;
        String indexNameFromAliasName = getIndexNameFromAliasName( client );
        while (indexNameFromAliasName != null) {
            IndicesAliasesRequestBuilder prepareAliases = client.admin().indices().prepareAliases();
            prepareAliases.removeAlias( indexNameFromAliasName, aliasName ).execute().actionGet();
            indexNameFromAliasName = getIndexNameFromAliasName( client );
        }
    }
    
    public static void deleteIndex(Client client, String index) {
        client.admin().indices().prepareDelete( index ).execute().actionGet();
    }

    public static void deleteType(Client client, String name) {
        client.admin().indices().prepareDeleteMapping( JettyStarter.getInstance().config.index ).setType( name ).execute().actionGet();
    }

    /**
     * Generate a new ID for a new index of the format <index-name>_<id>, where
     * <id> is number counting up.
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
                dateFormat.parse( name.substring( delimiterPos+1 ) );
            } catch (Exception ex) {
                isNew = true;
            }
        }
        
        String date = dateFormat.format( new Date() );

        if (isNew) {
            return name + "_" + date;
        } else {
            return name.substring( 0, delimiterPos+1 ) + date;
        }
    }

    public static void refreshIndex(Client client, String indexName) {
        client.admin().indices().refresh( new RefreshRequest( indexName ) ).actionGet();
    }

}
