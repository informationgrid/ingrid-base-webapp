/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.admin.Config;
import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.Utils;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.elasticsearch.StatusProvider.Classification;
import de.ingrid.admin.object.IDocumentProducer;
import de.ingrid.admin.service.PlugDescriptionService;
import de.ingrid.utils.ElasticDocument;
import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.tool.PlugDescriptionUtil;
import de.ingrid.utils.tool.QueryUtil;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class IndexRunnable implements Runnable, IConfigurable {

    private static final Logger LOG = Logger.getLogger( IndexRunnable.class );
    private List<IDocumentProducer> _documentProducers;
    private boolean _produceable = false;
    private PlugDescription _plugDescription;
    private final PlugDescriptionService _plugDescriptionService;
    private final IndexManager _indexManager;

    private final ConcurrentMap<String, Object> _indexHelper;

    @Autowired
    private StatusProvider statusProvider;

    @Autowired
    public IndexRunnable(PlugDescriptionService pds, IndexManager indexManager) throws Exception {
        _plugDescriptionService = pds;
        _indexManager = indexManager;
        _indexHelper = new ConcurrentHashMap<>();
    }

    @Autowired(required = false)
    public void setDocumentProducers(List<IDocumentProducer> documentProducers) {
        _documentProducers = documentProducers;
        _produceable = true;

        JettyStarter.getInstance().config.docProducerIndices = getIndexNamesFromProducers( documentProducers );
    }

    private String[] getIndexNamesFromProducers(List<IDocumentProducer> documentProducers) {
        List<String> indices = new ArrayList<>();

        for (IDocumentProducer docProducer : documentProducers) {
            IndexInfo indexInfo = docProducer.getIndexInfo();
            String currentIndex;
            String indexAlias;
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
                        indices.add( indexAlias );
                    }
                } else {
                    indices.add( indexAlias );
                }
            }
        }
        return indices.toArray( new String[0] );
    }

    @Override
    public void run() {
        _indexHelper.clear();
        if (_produceable) {
            // remember newIndex in case it has to be cleaned up, after an unsuccessful index process
            String newIndex = null;
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
                Map<String, String[]> indexNames = new HashMap<>();

                for (IDocumentProducer producer : _documentProducers) {
                    IndexInfo info = Utils.getIndexInfo( producer, config );
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

                    String stateKey = String.format("producer_%s_%s",
                            info.getToIndex(),
                            info.getToType());
                    String stateValue = String.format("Writing to Index: %s, Type: %s",
                            info.getToIndex(),
                            info.getToType());
                    this.statusProvider.addState(stateKey, stateValue);

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
    
                        _indexManager.addBasicFields( document, info );
    
                        this.statusProvider.addState(indexTag, "Indexing document: " + (count++) + indexPostfixString);
                        
                        _indexManager.update( info, document, false );

                        collectIndexFields( document );

                        documentCount++;
                    }

                    // update index now!
                    _indexManager.flush();

                    producer.configure( _plugDescription );
                }
                if (documentCount > 0) {
                    writeFieldNamesToPlugdescription();
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
                LOG.error( "Exception occurred during indexing: ", e );
                cleanUp(newIndex);
            } catch (Throwable t) {
                this.statusProvider.addState("error_indexing", "An exception occurred: " + t.getMessage() + ". Try increasing the HEAP-size or let it manage automatically.", Classification.ERROR);
                LOG.error( "Error during indexing", t );
                LOG.info( "Try increasing the HEAP-size or let it manage automatically." );
                cleanUp(newIndex);
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

    private void cleanUp(String newIndex) {
        if (JettyStarter.getInstance().config.alwaysCreateNewIndex && newIndex != null) {
            _indexManager.deleteIndex( newIndex );
        }
        statusProvider.addState( "CLEANUP", "Cleaned up data and reverted to old index" );
    }

    public boolean isProduceable() {
        return _produceable;
    }

    @Override
    public void configure(final PlugDescription plugDescription) {
        if (LOG.isDebugEnabled()) {
            LOG.debug( "configure plugdescription and new index dir..." );
        }
        _plugDescription = plugDescription;
    }

    public PlugDescription getPlugDescription() {
        return _plugDescription;
    }

    private void collectIndexFields(ElasticDocument ed) {
        for (Entry<String, Object> entry : ed.entrySet()) {
            String key = entry.getKey();
            if (key == null) {
                LOG.warn( "A key of an ElasticDocument was null, when collecting fields for PlugDescription" );
            } else {
                _indexHelper.putIfAbsent(key, "");
            }
        }
    }

    private void writeFieldNamesToPlugdescription() throws IOException {
        // first add "metainfo" field, so plug won't be filtered when field is
        // part of query !
        if (LOG.isInfoEnabled()) {
            LOG.info( "Add meta fields to PD." );
        }
        PlugDescriptionUtil.addFieldToPlugDescription( _plugDescription, QueryUtil.FIELDNAME_METAINFO );
        PlugDescriptionUtil.addFieldToPlugDescription( _plugDescription, QueryUtil.FIELDNAME_INCL_META );

        // then add fields from index
        if (LOG.isInfoEnabled()) {
            LOG.info( "Add fields from new index to PD." );
        }
        for (String property : _indexHelper.keySet()) {
            _plugDescription.addField( property );
            LOG.debug( String.format( "added index field %s to plugdescription.", property ) );
        }
    }

    public void setStatusProvider(StatusProvider statusProvider) {
        this.statusProvider = statusProvider;
    }
}
