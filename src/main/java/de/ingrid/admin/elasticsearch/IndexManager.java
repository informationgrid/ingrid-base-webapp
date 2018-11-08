/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequestBuilder;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkProcessor.Listener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.index.IndexNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.carrotsearch.hppc.cursors.ObjectCursor;

import de.ingrid.admin.Config;
import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.elasticsearch.StatusProvider.Classification;
import de.ingrid.admin.service.ElasticsearchNodeFactoryBean;
import de.ingrid.utils.ElasticDocument;
import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.xml.XMLSerializer;

@Service
public class IndexManager implements IConfigurable {
    private static final Logger LOG = Logger.getLogger( IndexManager.class );

    private Client _client;
    private BulkProcessor _bulkProcessor;

    private Config _config;

    private Properties _props = new Properties();
    
    @Autowired
    private StatusProvider statusProvider;

    @Autowired
    public IndexManager(ElasticsearchNodeFactoryBean elastic) throws Exception {
        _client = elastic.getClient();
        _bulkProcessor = BulkProcessor.builder( _client, getBulkProcessorListener() ).build();
        _config = JettyStarter.getInstance().config;
    }

    /**
     * Insert or update a document to the lucene index. For updating the documents must
     * be indexed by its ID and the global configuration "indexWithAutoId" (index.autoGenerateId)
     * has to be disabled.
     * @param indexinfo contains information about the index to be used besided other information
     * @param doc is the document to be indexed
     * @param updateOldIndex if true, it'll be checked if the current index differs from the real index, which is used during reindexing
     */
    public void update(IndexInfo indexinfo, ElasticDocument doc, boolean updateOldIndex) {
        IndexRequest indexRequest = new IndexRequest();
        indexRequest.index( indexinfo.getRealIndexName() ).type( indexinfo.getToType() );

        if (!_config.indexWithAutoId) {
            indexRequest.id( (String) doc.get( indexinfo.getDocIdField() ) );
        }

        _bulkProcessor.add( indexRequest.source( doc ) );
        
        if (updateOldIndex) {
            String oldIndex = getIndexNameFromAliasName(indexinfo.getToAlias(), null);
            // if the current index differs from the real index, then it means there's an indexing going on
            // and if the real index name is the same as the index alias, it means that no complete indexing happened yet
            if ((oldIndex != null) && !oldIndex.equals( indexinfo.getRealIndexName() ) && (!indexinfo.getToIndex().equals( indexinfo.getRealIndexName()) )) {
                IndexInfo otherIndexInfo = indexinfo.clone();
                otherIndexInfo.setRealIndexName( oldIndex );
                update( otherIndexInfo, doc, false );
            }
        }
    }
    
    /**
     * Delete a document with a given ID from an index/type. The ID must not be
     * autogenerated but a unique ID from the source document.
     * @param indexinfo
     * @param id
     * @param updateOldIndex TODO
     */
    public void delete(IndexInfo indexinfo, String id, boolean updateOldIndex) {
        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.index( indexinfo.getRealIndexName() ).type( indexinfo.getToType() ).id( id );
        
        _bulkProcessor.add( deleteRequest );
        
        if (updateOldIndex) {
            String oldIndex = getIndexNameFromAliasName(indexinfo.getToAlias(), null);
            if (!oldIndex.equals( indexinfo.getRealIndexName() )) {
                IndexInfo otherIndexInfo = indexinfo.clone();
                otherIndexInfo.setRealIndexName( oldIndex );
                delete( otherIndexInfo, id, false );
            }
        }
    }

    private Listener getBulkProcessorListener() {
        return new BulkProcessor.Listener() {

            @Override
            public void beforeBulk(long executionId, BulkRequest request) {}

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable t) {
                LOG.error( "An error occured during bulk indexing", t );
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {}
        };
    }

    public void flush() {
        _bulkProcessor.flush();
    }

    public void flushAndClose() {
        _bulkProcessor.flush();
        _bulkProcessor.close();
    }
    
    public void switchAlias(String aliasName, String oldIndex, String newIndex) {
        // check if alias actually exists
        // boolean aliasExists = _client.admin().indices().aliasesExist( new GetAliasesRequest( aliasName ) ).actionGet().exists();
        if (oldIndex != null) removeFromAlias(aliasName, oldIndex);
        IndicesAliasesRequestBuilder prepareAliases = _client.admin().indices().prepareAliases();
        prepareAliases.addAlias( newIndex, aliasName ).execute().actionGet();
    }

    public void addToAlias(String aliasName, String newIndex) {
        IndicesAliasesRequestBuilder prepareAliases = _client.admin().indices().prepareAliases();
        prepareAliases.addAlias( newIndex, aliasName ).execute().actionGet();
    }

    public void removeFromAlias(String aliasName, String index) {
        String indexNameFromAliasName = getIndexNameFromAliasName(aliasName, index);
        while (indexNameFromAliasName != null) {
            IndicesAliasesRequestBuilder prepareAliases = _client.admin().indices().prepareAliases();
            prepareAliases.removeAlias( indexNameFromAliasName, aliasName ).execute().actionGet();
            indexNameFromAliasName = getIndexNameFromAliasName(aliasName, index);
        }
    }
    
    public void removeAlias(String aliasName) {
        removeFromAlias( aliasName, null );
    }
  
    public boolean typeExists(String indexName, String type) {
        TypesExistsRequest typeRequest = new TypesExistsRequest( new String[] { indexName }, type );
        try {
            boolean typeExists = _client.admin().indices().typesExists( typeRequest ).actionGet().isExists();
            return typeExists;
        } catch (IndexNotFoundException e) {
            return false;
        }
    }

    public void deleteIndex(String index) {
        _client.admin().indices().prepareDelete( index ).execute().actionGet();
    }

    public boolean createIndex(String name) {
        boolean indexExists = indexExists( name );
        if (!indexExists) {
            CreateIndexRequestBuilder prepareCreate = _client.admin().indices().prepareCreate( name );
            setSettings( prepareCreate );
            setMapping( prepareCreate );
            prepareCreate.execute().actionGet();

            return true;
        }
        return false;
    }
    
    private void setMapping(CreateIndexRequestBuilder prepareCreate) {
        InputStream mappingStream = getClass().getClassLoader().getResourceAsStream("default-mapping.json");
        if (mappingStream != null) {
            String source;
            try {
                source = XMLSerializer.getContents( mappingStream );
                prepareCreate.addMapping( "_default_", source );
            } catch (IOException e) {
                LOG.error( "Could not update settings to index", e );
            }
        }
    }
    
    private void setSettings(CreateIndexRequestBuilder prepareCreate) {
        InputStream mappingStream = getClass().getClassLoader().getResourceAsStream("default-settings.json");
        if (mappingStream != null) {
            String source;
            try {
                source = XMLSerializer.getContents( mappingStream );
                prepareCreate.setSettings( source );
            } catch (IOException e) {
                LOG.error( "Could not update settings to index", e );
            }
        }
    }
    
    public boolean indexExists(String name) {
        return _client.admin().indices().prepareExists( name ).execute().actionGet().isExists();
    }

    /**
     * Get the index matching the partial name from an alias.
     * @param indexAlias is the alias name to check for connected indices
     * @param partialName is the first part of the index to be matched, if there are several
     * @return the found index name
     */
    public String getIndexNameFromAliasName(String indexAlias, String partialName) {
        
        ImmutableOpenMap<String, List<AliasMetaData>> indexToAliasesMap = _client.admin().indices().getAliases(new GetAliasesRequest(indexAlias)).actionGet().getAliases();
        
        if (indexToAliasesMap != null && !indexToAliasesMap.isEmpty()) {
            Iterator<ObjectCursor<String>> iterator = indexToAliasesMap.keys().iterator();
            String result = null;
            int count = 0;
            while (iterator.hasNext()) {
                String next = iterator.next().value;
                if (partialName == null || next.indexOf( partialName ) != -1) {
                    result = next;
                    count++;
                }
            }
            
            if (count > 1) {
                this.statusProvider.addState( "MULTIPLE_INDICES", "The index name could not be determined correctly, since the alias contains " + count + " indices with the same prefix '" + partialName + "'", Classification.ERROR );
            }
            
            return result;
        } else if (_client.admin().indices().prepareExists(indexAlias).execute().actionGet().isExists()) {
            // alias seems to be the index itself
            return indexAlias;
        }
        return null;
    }

    public MappingMetaData getMapping(IndexInfo indexInfo) {
        String indexName = getIndexNameFromAliasName(indexInfo.getRealIndexName(), null);
        ClusterState cs = _client.admin().cluster().prepareState().setIndices( indexName ).execute().actionGet().getState();
        return cs.getMetaData().index( indexName ).mapping( indexInfo.getToType() );
    }

    public void refreshIndex(String indexName) {
        _client.admin().indices().refresh( new RefreshRequest( indexName ) ).actionGet();
    }

    public Client getClient() {
        return _client;
    }

    public String printSettings() throws Exception {
        return _client.settings().toDelimitedString( ',' );
    }

    public void shutdown() throws Exception {
        _client.close();
    }

    public void addBasicFields(ElasticDocument document, IndexInfo info) {
        String identifier = info.getIdentifier();
        String datatypes = (String) _props.get( "plugdescription.dataType." + identifier );
        String partner = (String) _props.get( "plugdescription.partner." + identifier );
        String provider = (String) _props.get( "plugdescription.provider." + identifier );
        
        if (datatypes != null) {
            document.put( "datatype", datatypes.split( "," ) );
        } else {
            document.put( "datatype", _config.datatypes.toArray( new String[0] ) );
        } 
        
        if (partner != null) {
            document.put( "partner", partner.split( "," ) );
        } else {
            document.put( "partner", _config.partner );
        }
        
        if (provider != null) {
            document.put( "provider", provider.split( "," ) );
        } else {
            document.put( "provider", _config.provider );
        }
    }

    @Override
    public void configure(PlugDescription plugDescription) {
        try {
            this._props = _config.getOverrideProperties();
        } catch (IOException e) {
            LOG.error( "Error reading override configuration.", e );
            e.printStackTrace();
        }
    }
    
    public void setStatusProvider(StatusProvider statusProvider) {
        this.statusProvider = statusProvider;
    }
}
