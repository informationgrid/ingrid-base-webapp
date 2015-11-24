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
package de.ingrid.admin.elasticsearch;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkProcessor.Listener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.admin.Config;
import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.object.IDocumentProducer;
import de.ingrid.admin.service.ElasticsearchNodeFactoryBean;
import de.ingrid.admin.service.PlugDescriptionService;
import de.ingrid.utils.ElasticDocument;
import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.tool.PlugDescriptionUtil;
import de.ingrid.utils.tool.QueryUtil;

@Service
public class IndexRunnable implements Runnable, IConfigurable {

    private static final Logger LOG = Logger.getLogger( IndexRunnable.class );
    private static final int RETRIES_FETCH_MAPPING = 10;
    private int _documentCount;
    private List<IDocumentProducer> _documentProducers;
    private boolean _produceable = false;
    private PlugDescription _plugDescription;
    private final PlugDescriptionService _plugDescriptionService;
    private String[] _dataTypes;
    private Client _client;

    @Autowired
    public IndexRunnable(ElasticsearchNodeFactoryBean elastic, PlugDescriptionService pds) throws Exception {
        _client = elastic.getObject().client();
        _plugDescriptionService = pds;
    }

    @Autowired(required = false)
    public void setDocumentProducers(List<IDocumentProducer> documentProducers) {
        _documentProducers = documentProducers;
        _produceable = true;
    }

    public void run() {
        if (_produceable) {
            try {
                LOG.info( "indexing starts" );
                resetDocumentCount();
                BulkProcessor bulkProcessor = BulkProcessor.builder( _client, getBulkProcessorListener() ).build();
                Config config = JettyStarter.getInstance().config;
                
                // get the current index from the alias name
                // if it's the first time then use the name given by the
                // configuration
                String oldIndex = ElasticSearchUtils.getIndexNameFromAliasName( _client );
                String newIndex = ElasticSearchUtils.getNextIndexName( oldIndex == null ? config.index : oldIndex );
                if (config.indexWithAutoId) {
                    ElasticSearchUtils.createIndex( _client, newIndex );
                }

                for (IDocumentProducer producer : _documentProducers) {
                    IndexInfo info = getIndexInfo( producer, config );
                    while (producer.hasNext()) {
                        final ElasticDocument document = producer.next();
                        if (document == null) {
                            LOG.warn( "DocumentProducer " + producer + " returned null Document, we skip this record (not added to index)!" );
                            continue;
                        }
    
                        document.put( "datatype", _dataTypes );
                        document.put( "partner", config.partner );
                        document.put( "provider", config.provider );
    
                        if (_documentCount % 50 == 0) {
                            LOG.info( "add document to index: " + _documentCount );
                        }
    
                        // index into a temporary
                        // index and switch the old with the new one at the end
                        IndexRequest indexRequest = new IndexRequest();
                        if (config.indexWithAutoId) {
                            indexRequest.index( newIndex )
                                .type( info.getToType() );
                        } else {
                            indexRequest.index( info.getToIndex() )
                                .type( info.getToType() )
                                .id( (String) document.get( info.getDocIdField() ) );
                        }
                        bulkProcessor.add( indexRequest.source( document ) );
                        _documentCount++;
                    }
                    producer.configure( _plugDescription );
                }
                
                LOG.info( "number of produced documents: " + _documentCount );
                bulkProcessor.flush();
                bulkProcessor.close();
                LOG.info( "indexing ends" );

                if (config.indexWithAutoId) {
                    ElasticSearchUtils.switchAlias( _client, newIndex );
                    if (oldIndex != null) {
                        ElasticSearchUtils.deleteIndex( _client, oldIndex );
                    }
                    LOG.info( "switched alias to new index and deleted old one" );
                } else {
                    // TODO: remove documents which have not been updated (hence removed!)
                }

                // Extend PD with all field names in index and save
                addFieldNamesToPlugdescription( _client, config, _plugDescription, RETRIES_FETCH_MAPPING );

                // update new fields into override property
                PlugdescriptionCommandObject pdObject = new PlugdescriptionCommandObject();
                pdObject.putAll( _plugDescription );
                config.writePlugdescriptionToProperties( pdObject );

                _plugDescriptionService.savePlugDescription( _plugDescription );

            } catch (final Exception e) {
                e.printStackTrace();
            } catch (Throwable t) {
                LOG.error( "Error during indexing", t );
                LOG.info( "Try increasing the HEAP-size or let it manage automatically." );
            } finally {
                resetDocumentCount();
            }
        } else {
            LOG.warn( "configuration fails. disable index creation." );
        }

    }

    private IndexInfo getIndexInfo(IDocumentProducer producer, Config config) {
        IndexInfo indexInfo = producer.getIndexInfo();
        if (indexInfo == null) {
            indexInfo = new IndexInfo();
            indexInfo.setToIndex( config.index );
            indexInfo.setToType( config.indexType );
            indexInfo.setDocIdField( config.indexIdFromDoc );
        }
        return indexInfo;
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

    private void resetDocumentCount() {
        _documentCount = 0;
    }

    public int getDocumentCount() {
        return _documentCount;
    }

    public boolean isProduceable() {
        return _produceable;
    }

    public void configure(final PlugDescription plugDescription) {
        if (LOG.isDebugEnabled()) {
            LOG.debug( "configure plugdescription and new index dir..." );
        }
        resetDocumentCount();
        _plugDescription = plugDescription;
        _dataTypes = plugDescription.getDataTypes();
    }

    public PlugDescription getPlugDescription() {
        return _plugDescription;
    }

    /** Add all field names of the given index to the given plug description ! */
    public static void addFieldNamesToPlugdescription(Client client, Config config, PlugDescription pd, int retries) throws IOException {
        // remove all fields
        if (LOG.isInfoEnabled()) {
            LOG.info( "New Index, remove all field names from PD." );
        }
        pd.remove( PlugDescription.FIELDS );

        // first add "metainfo" field, so plug won't be filtered when field is
        // part of query !
        if (LOG.isInfoEnabled()) {
            LOG.info( "Add meta fields to PD." );
        }
        PlugDescriptionUtil.addFieldToPlugDescription( pd, QueryUtil.FIELDNAME_METAINFO );
        PlugDescriptionUtil.addFieldToPlugDescription( pd, QueryUtil.FIELDNAME_INCL_META );

        // then add fields from index
        if (LOG.isInfoEnabled()) {
            LOG.info( "Add fields from new index to PD." );
        }

        String indexName = ElasticSearchUtils.getIndexNameFromAliasName( client );
        
        // get the fields from the mapping, which is updated after each indexing
        ClusterState cs = client.admin().cluster().prepareState().setIndices( indexName ).execute().actionGet().getState();
        MappingMetaData mdd = cs.getMetaData().index( indexName ).mapping( config.indexType );
        
        if (mdd == null && retries > 0) {
            LOG.warn( "Cluster state was not ready yet for fetching mapping ... waiting 1s" );
            try {
                // since this thread is independent from the others (e.g. search), we can allow to let it sleep
                Thread.sleep( 1000 );
            } catch (InterruptedException e) {
                LOG.error( "Thread has been interrupted, while waiting for ClusterState" );
                e.printStackTrace();
            }
            addFieldNamesToPlugdescription(client, config, pd, --retries);
            return;
        }
        
        if (mdd == null) {
            LOG.error( "Mapping metadata was not received. PlugDescription won't be updated with indexed fields." );
            return;
        }

        @SuppressWarnings("unchecked")
        ElasticDocument fields = new ElasticDocument( (Map<String, Object>) mdd.getSourceAsMap().get( "properties" ) );
        Set<String> propertiesSet = fields.keySet();

        for (String property : propertiesSet) {
            pd.addField( property );
            if (LOG.isDebugEnabled()) {
                LOG.debug( "added index field " + property + " to plugdescription." );
            }
        }

    }
}
