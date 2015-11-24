package de.ingrid.admin.elasticsearch;

import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkProcessor.Listener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.springframework.beans.factory.annotation.Autowired;

import de.ingrid.admin.Config;
import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.service.ElasticsearchNodeFactoryBean;
import de.ingrid.utils.ElasticDocument;

public class IndexManager {
    private static final Logger LOG = Logger.getLogger( IndexManager.class );

    private Client _client;
    private BulkProcessor _bulkProcessor;

    private Config _config;

    private ElasticsearchNodeFactoryBean _elastic;

    @Autowired
    public IndexManager(ElasticsearchNodeFactoryBean elastic) throws Exception {
        _elastic = elastic;
        _client = elastic.getObject().client();
        _bulkProcessor = BulkProcessor.builder( _client, getBulkProcessorListener() ).build();
        _config = JettyStarter.getInstance().config;
    }

    public void update(IndexInfo indexinfo, ElasticDocument doc) {
        IndexRequest indexRequest = new IndexRequest();
        indexRequest.index( indexinfo.getToIndex() ).type( indexinfo.getToType() );

        if (!_config.indexWithAutoId) {
            indexRequest.id( (String) doc.get( indexinfo.getDocIdField() ) );
        }

        _bulkProcessor.add( indexRequest.source( doc ) );
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

    public void switchAlias(String newIndex) {
        String aliasName = _config.index;
        removeAlias();
        IndicesAliasesRequestBuilder prepareAliases = _client.admin().indices().prepareAliases();
        prepareAliases.addAlias( newIndex, aliasName ).execute().actionGet();
    }

    public void removeAlias() {
        String aliasName = _config.index;
        String indexNameFromAliasName = getIndexNameFromAliasName( _client );
        while (indexNameFromAliasName != null) {
            IndicesAliasesRequestBuilder prepareAliases = _client.admin().indices().prepareAliases();
            prepareAliases.removeAlias( indexNameFromAliasName, aliasName ).execute().actionGet();
            indexNameFromAliasName = getIndexNameFromAliasName( _client );
        }
    }

    private String getIndexNameFromAliasName(Client _client2) {
        ImmutableOpenMap<String, AliasMetaData> indexToAliasesMap = _client.admin().cluster().state( Requests.clusterStateRequest() )
                .actionGet().getState().getMetaData().aliases()
                .get( JettyStarter.getInstance().config.index );
        if (indexToAliasesMap != null && !indexToAliasesMap.isEmpty()) {
            return indexToAliasesMap.keys().iterator().next().value;
        }
        return null;
    }
    
    public boolean typeExists(String type) {
        TypesExistsRequest typeRequest = new TypesExistsRequest( new String[] { _config.index }, type );
        boolean typeExists = _client.admin().indices().typesExists( typeRequest ).actionGet().isExists();
        return typeExists;
    }

    public void deleteIndex(String index) {
        _client.admin().indices().prepareDelete( index ).execute().actionGet();
    }

    public boolean createIndex(String name) {
        boolean indexExists = _client.admin().indices().prepareExists( name ).execute().actionGet().isExists();
        if (!indexExists) {
            _client.admin().indices().prepareCreate( name ).execute().actionGet();
            return true;
        }
        return false;
    }

    public String getIndexNameFromAliasName() {
        ImmutableOpenMap<String, AliasMetaData> indexToAliasesMap = _client.admin().cluster().state( Requests.clusterStateRequest() )
                .actionGet().getState().getMetaData().aliases()
                .get( _config.index );
        if (indexToAliasesMap != null && !indexToAliasesMap.isEmpty()) {
            return indexToAliasesMap.keys().iterator().next().value;
        }
        return null;
    }

    public MappingMetaData getMapping(IndexInfo indexInfo) {
        String indexName = getIndexNameFromAliasName();
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
        return _elastic.getObject().settings().toDelimitedString( ',' );
    }

    public void shutdown() throws Exception {
        _elastic.getObject().close();
    }

}
