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

import java.io.IOException;
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
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.admin.Config;
import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.object.IDocumentProducer;
import de.ingrid.admin.service.ElasticsearchNodeFactoryBean;
import de.ingrid.admin.service.PlugDescriptionService;
import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.tool.PlugDescriptionUtil;
import de.ingrid.utils.tool.QueryUtil;

@Service
public class IndexRunnable implements Runnable, IConfigurable {

    private static final Logger LOG = Logger.getLogger( IndexRunnable.class );
    private int _documentCount;
    private IDocumentProducer _documentProducer;
    //private Directory _indexDir;
    private boolean _produceable = false;
    private PlugDescription _plugDescription;
    //private final IConfigurable _ingridIndexSearcher;
    private final PlugDescriptionService _plugDescriptionService;
    //private final Stemmer _stemmer;
    private String[] _dataTypes;
    private Client _client;

    @Autowired
    public IndexRunnable(ElasticsearchNodeFactoryBean elastic, PlugDescriptionService pds) throws Exception {
        _client = elastic.getObject().client();
        _plugDescriptionService = pds;
    }

    @Autowired(required = false)
    public void setDocumentProducer(final IDocumentProducer documentProducer) {
        _documentProducer = documentProducer;
        _produceable = true;
    }

    @SuppressWarnings("unchecked")
    public void run() {
        if (_produceable) {
            try {
                LOG.info( "indexing starts" );
                resetDocumentCount();
                //final IndexWriter writer = new IndexWriter( _indexDir, _stemmer.getAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED );
                BulkProcessor bulkProcessor = BulkProcessor.builder( _client, getBulkProcessorListener() ).build();
                Config config = JettyStarter.getInstance().config;
                
                while (_documentProducer.hasNext()) {
                    final Map<String, Object> document = _documentProducer.next();
                    if (document == null) {
                        LOG.warn( "DocumentProducer " + _documentProducer + " returned null Document, we skip this record (not added to index)!" );
                        continue;
                    }

                    //for (final String dataType : _dataTypes) {
                        document.put( "datatype", _dataTypes );
                    //}
                    if (_documentCount % 50 == 0) {
                        LOG.info( "add document to index: " + _documentCount );
                    }

                    IndexRequest indexRequest = new IndexRequest( config.index, config.indexType );
                    if (!config.indexWithAutoId) {
                        indexRequest.id( (String)document.get( config.indexIdFromDoc ) );
                    }
                    bulkProcessor.add(indexRequest.source(document));
                    _documentCount++;
                }
                LOG.info( "number of produced documents: " + _documentCount );
                bulkProcessor.flush();
                bulkProcessor.close();
                LOG.info( "indexing ends" );

                // Extend PD with all field names in index and save
                addFieldNamesToPlugdescription( _client, config, _plugDescription );

                // update new fields into override property
                PlugdescriptionCommandObject pdObject = new PlugdescriptionCommandObject();
                pdObject.putAll( _plugDescription );
                config.writePlugdescriptionToProperties( pdObject );

                _plugDescriptionService.savePlugDescription( _plugDescription );

//                _ingridIndexSearcher.configure( _plugDescription );
                _documentProducer.configure( _plugDescription );
            } catch (final Exception e) {
                e.printStackTrace();
            } finally {
                resetDocumentCount();
            }
        } else {
            LOG.warn( "configuration fails. disable index creation." );
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
//        if (_plugDescription != null) {
//            final File workinDirectory = _plugDescription.getWorkinDirectory();
//            final File indexDir = new File( workinDirectory, "newIndex" );
//            try {
//                _indexDir = FSDirectory.open( indexDir );
//            } catch (IOException ex) {
//                LOG.error( "Problems creating directory for new index: " + indexDir, ex );
//            }
//        }
        // run();
    }

    public PlugDescription getPlugDescription() {
        return _plugDescription;
    }

//    public IngridIndexSearcher getIngridIndexSearcher() {
//        return (IngridIndexSearcher) _ingridIndexSearcher;
//    }

    /** Add all field names of the given index to the given plug description ! */
    public static void addFieldNamesToPlugdescription(Client client, Config config, PlugDescription pd) throws IOException {
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
        
        // get the fields from the mapping, which is updated after each indexing
        ClusterState cs = client.admin().cluster().prepareState().setIndices( config.index ).execute().actionGet().getState();
        IndexMetaData imd = cs.getMetaData().index( config.index );
        MappingMetaData mdd = imd.mapping( config.indexType );
        
        // TODO: maybe wait for index refreshed, or better refresh it itself!
        
        @SuppressWarnings("unchecked")
        Map<String, Object> fields = (Map<String, Object>) mdd.getSourceAsMap().get( "properties" );
        Set<String> propertiesSet = fields.keySet();

        for (String property : propertiesSet) {
            pd.addField( property);
            if (LOG.isDebugEnabled()) {
                LOG.debug( "added index field " + property + " to plugdescription." );
            }
        }
        
    }
}
