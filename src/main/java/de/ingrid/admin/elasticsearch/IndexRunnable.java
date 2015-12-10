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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.admin.Config;
import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.elasticsearch.StatusProvider.Classification;
import de.ingrid.admin.object.IDocumentProducer;
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
    private List<IDocumentProducer> _documentProducers;
    private boolean _produceable = false;
    private PlugDescription _plugDescription;
    private final PlugDescriptionService _plugDescriptionService;
    private IndexManager _indexManager;
    
    @Autowired
    private StatusProvider statusProvider;

    @Autowired
    public IndexRunnable(PlugDescriptionService pds, IndexManager indexManager) throws Exception {
        _plugDescriptionService = pds;
        _indexManager = indexManager;
    }

    @Autowired(required = false)
    public void setDocumentProducers(List<IDocumentProducer> documentProducers) {
        _documentProducers = documentProducers;
        _produceable = true;
        
        JettyStarter.getInstance().config.docProducerIndices = getIndexNamesFromProducers( documentProducers );
    }

    private String[] getIndexNamesFromProducers(List<IDocumentProducer> documentProducers) {
        ArrayList<String> indices = new ArrayList<String>();
        
        for (IDocumentProducer docProducer : documentProducers) {
            IndexInfo indexInfo = docProducer.getIndexInfo();
            String currentIndex = null;
            String indexAlias = null;
            if (indexInfo == null) {
                indexAlias = JettyStarter.getInstance().config.index;
            } else {
                indexAlias = indexInfo.getToIndex();
            }

            if (!indices.contains( indexAlias )) {
                currentIndex = _indexManager.getIndexNameFromAliasName( indexAlias );
                if (currentIndex == null) {
                    String nextIndexName = ElasticSearchUtils.getNextIndexName( indexAlias );
                    boolean wasCreated = _indexManager.createIndex( nextIndexName );
                    if (wasCreated) {
                        _indexManager.switchAlias( indexAlias, nextIndexName );
                    }
                } else {
                    indices.add( indexAlias );
                }
            }
        }
        return indices.toArray( new String[0] );
    }

    public void run() {
        if (_produceable) {
            try {
                statusProvider.clear();
                statusProvider.addState( "start_indexing", "Start indexing");
                LOG.info( "indexing starts" );
                Config config = JettyStarter.getInstance().config;
                
                // remove all fields from plug description
                if (LOG.isInfoEnabled()) {
                    LOG.info( "New Index, remove all field names from PD." );
                }
                _plugDescription.remove( PlugDescription.FIELDS );
                
                int documentCount = 0;
                String oldIndex = null;
                String newIndex = null;
                Map<String, String[]> indexNames = new HashMap<String, String[]>();

                for (IDocumentProducer producer : _documentProducers) {
                    IndexInfo info = getIndexInfo( producer, config );
                    // get the current index from the alias name
                    // if it's the first time then use the name given by the
                    // configuration
                    // only create new index if we did not already ... this depends on the producer settings
                    if (!indexNames.containsKey( info.getToIndex() )) {
                        oldIndex = _indexManager.getIndexNameFromAliasName(info.getToIndex());
                        newIndex = ElasticSearchUtils.getNextIndexName( oldIndex == null ? info.getToIndex() : oldIndex );
                        if (config.alwaysCreateNewIndex) {
                            _indexManager.createIndex( newIndex );
                        }
                        indexNames.put( info.getToIndex(), new String[] { oldIndex, newIndex } );
                    }
                    if (config.alwaysCreateNewIndex) {
                        info.setRealIndexName( newIndex );
                    } else {
                        info.setRealIndexName( oldIndex );
                    }
                    
                    this.statusProvider.addState("producer_" + info.getToIndex() + "_" + info.getToType(), "Writing to Index: " + info.getToIndex() + " -> " + info.getToType());
                    
                    int count = 1, skip = 0;
                    Integer totalCount = producer.getDocumentCount();
                    String indexPostfixString = totalCount == null ? "" : " / " + totalCount;
                    String indexTag = "indexing_" + info.getToIndex() + "_" + info.getToType();
                    while (producer.hasNext()) {
                        final ElasticDocument document = producer.next();
                        if (document == null) {
                            LOG.warn( "DocumentProducer " + producer + " returned null Document, we skip this record (not added to index)!" );
                            this.statusProvider.addState(indexTag + "_skipped", "Skipped documents: " + (++skip), Classification.WARN);
                            continue;
                        }
    
                        _indexManager.addBasicFields( document );
    
                        this.statusProvider.addState(indexTag, "Indexing document: " + (count++) + indexPostfixString);
                        
                        _indexManager.update( info, document, false );
                        documentCount++;
                    }
                    
                    // update index now!
                    _indexManager.flush();
                    
                    // Extend PD with all field names in index and save
                    addFieldNamesToPlugdescription( info, _plugDescription, RETRIES_FETCH_MAPPING );
                    
                    producer.configure( _plugDescription );
                }
                
                LOG.info( "number of produced documents: " + documentCount );
                
                LOG.info( "indexing ends" );

                if (config.alwaysCreateNewIndex) {
                    // switch aliases of all document producers to the new indices
                    for (String index : indexNames.keySet()) {
                        String[] indexMore = indexNames.get( index );
                        _indexManager.switchAlias( index, indexMore[1]);
                        if (oldIndex != null) {
                            _indexManager.deleteIndex( indexMore[0] );
                        }
                        this.statusProvider.addState("switch_index", "Switch to newly created index: " + indexMore[1]);
                    }
                    LOG.info( "switched alias to new index and deleted old one" );
                } else {
                    // TODO: remove documents which have not been updated (hence removed!)
                }
                
                this.statusProvider.addState("stop_indexing", "Indexing finished.");

                // update new fields into override property
                PlugdescriptionCommandObject pdObject = new PlugdescriptionCommandObject();
                pdObject.putAll( _plugDescription );
                config.writePlugdescriptionToProperties( pdObject );

                _plugDescriptionService.savePlugDescription( _plugDescription );

            } catch (final Exception e) {
                this.statusProvider.addState("error_indexing", "An exception occurred: " + e.getMessage(), Classification.ERROR);
                e.printStackTrace();
            } catch (Throwable t) {
                this.statusProvider.addState("error_indexing", "An exception occurred: " + t.getMessage() + ". Try increasing the HEAP-size or let it manage automatically.", Classification.ERROR);
                LOG.error( "Error during indexing", t );
                LOG.info( "Try increasing the HEAP-size or let it manage automatically." );
            } finally {
                try {
                    this.statusProvider.write();
                } catch (IOException e) {
                    LOG.error( "Could not write status provider file", e );
                }
            }
        } else {
            LOG.warn( "configuration fails. disable index creation." );
        }

    }

    public IndexInfo getIndexInfo(IDocumentProducer producer, Config config) {
        IndexInfo indexInfo = producer.getIndexInfo();
        if (indexInfo == null) {
            indexInfo = new IndexInfo();
            indexInfo.setToIndex( config.index );
            indexInfo.setToType( config.indexType );
            indexInfo.setDocIdField( config.indexIdFromDoc );
        }
        return indexInfo;
    }

    public boolean isProduceable() {
        return _produceable;
    }

    public void configure(final PlugDescription plugDescription) {
        if (LOG.isDebugEnabled()) {
            LOG.debug( "configure plugdescription and new index dir..." );
        }
        _plugDescription = plugDescription;
    }

    public PlugDescription getPlugDescription() {
        return _plugDescription;
    }

    /** Add all field names of the given index to the given plug description ! */
    private void addFieldNamesToPlugdescription(IndexInfo indexInfo, PlugDescription pd, int retries) throws IOException {
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

        // get the fields from the mapping, which is updated after each indexing
        MappingMetaData mdd = _indexManager.getMapping(indexInfo);
        
        if (mdd == null && retries > 0) {
            LOG.warn( "Cluster state was not ready yet for fetching mapping ... waiting 1s" );
            try {
                // since this thread is independent from the others (e.g. search), we can allow to let it sleep
                Thread.sleep( 1000 );
            } catch (InterruptedException e) {
                LOG.error( "Thread has been interrupted, while waiting for ClusterState" );
                e.printStackTrace();
            }
            addFieldNamesToPlugdescription(indexInfo, pd, --retries);
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
    
    public void setStatusProvider(StatusProvider statusProvider) {
        this.statusProvider = statusProvider;
    }
}
