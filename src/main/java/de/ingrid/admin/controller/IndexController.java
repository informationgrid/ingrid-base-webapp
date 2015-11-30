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
package de.ingrid.admin.controller;

import java.io.IOException;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.count.CountRequest;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;
import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.elasticsearch.IndexManager;
import de.ingrid.admin.elasticsearch.IndexRunnable;
import de.ingrid.admin.object.IDocumentProducer;

@Controller
public class IndexController extends AbstractController {

    private Thread _thread = null;
    private final IndexRunnable _indexRunnable;
    private static final Log LOG = LogFactory.getLog(IndexController.class);
    private IndexManager indexManager;
    private List<IDocumentProducer> docProducer;

    @Autowired
    public IndexController(final IndexRunnable indexRunnable, IndexManager indexManager, List<IDocumentProducer> docProducer) throws Exception {
        _indexRunnable = indexRunnable;
        _thread = new Thread(indexRunnable);
        this.indexManager = indexManager;
        this.docProducer = docProducer;
    }

    @ModelAttribute("state")
    public State injectState() {
        return !_indexRunnable.isProduceable() ? State.BLOCKED : _thread.getState();
    }

    @ModelAttribute("documentCount")
    public int injectDocumentCount() {
        int documentCount = 0;
        if (_indexRunnable != null) {
        	documentCount = _indexRunnable.getDocumentCount();
        }
        return documentCount;
    }

    @RequestMapping(value = IUris.INDEXING, method = RequestMethod.GET)
    public String getIndexing(ModelMap model) {
        return IViews.INDEXING;
    }

    @RequestMapping(value = IUris.INDEXING, method = RequestMethod.POST)
    public String postIndexing(ModelMap model) throws Exception {
        if (_indexRunnable.isProduceable()) {
            if (_thread.getState() == State.NEW) {
                LOG.info("start indexer");
                _thread.start();
            } else if (_thread.getState() == State.TERMINATED) {
                LOG.info("start indexer");
                _thread = new Thread(_indexRunnable);
                _thread.start();
            } else {
                LOG.info("indexer was not started");
            }
        } else {
            LOG.warn("can not start indexer, because it is not produceable");
        }

        model.addAttribute("started", true);
        return IViews.INDEXING;

    }
    
    @RequestMapping(value = IUris.INDEX_STATE, method = RequestMethod.GET)
    public String getIndexState(ModelMap model){
    	model.addAttribute("state", _thread.getState());
    	return "/base/indexState";
    }
    
    @RequestMapping(value = IUris.INDEX_STATUS, method = RequestMethod.GET)
    public String getIndexStatus(final ModelMap modelMap) throws Exception {
        // get cluster health information
        ClusterHealthResponse clusterHealthResponse = indexManager.getClient().admin().cluster().health( new ClusterHealthRequest() ).get();

        // get all indices
        List<IndexStatus> indices = new ArrayList<IndexStatus>();
        for (IDocumentProducer producer : docProducer) {
            
            String index = producer.getIndexInfo().getToIndex();
            String indexType = producer.getIndexInfo().getToType();
            
            String currentIndex = indexManager.getIndexNameFromAliasName(index);
            if (currentIndex == null) continue;
    
            // get mapping
            GetMappingsResponse mappingResponse = indexManager.getClient().admin().indices().getMappings( new GetMappingsRequest() ).get();
            ImmutableOpenMap<String, MappingMetaData> mapping = mappingResponse.getMappings().get( currentIndex );
            
            long count = indexManager.getClient().count( new CountRequest( currentIndex ).types( indexType ) ).get().getCount();

            Object mappingAsString = mapping.get( indexType ) != null ? mapping.get( indexType ).source() : "\"No mapping exists!\"";
            IndexStatus indexStatus = new IndexStatus( currentIndex, indexType, count, mappingAsString );
            indices.add( indexStatus );
        }
        modelMap.addAttribute( "clusterState", clusterHealthResponse );
        modelMap.addAttribute( "indices", indices );
        return IViews.INDEX_STATUS;
    }

    @RequestMapping(value = IUris.INDEX_STATUS, method = RequestMethod.POST)
    public String setIndexStatus(@RequestParam("action") final String action) throws IOException {
        return redirect( IUris.INDEX_STATUS );
    }
    
    public class IndexStatus {
        private String indexName;
        private String indexType;
        private long docCount;
        private Object mapping;
        
        public IndexStatus(String name, String type, long count, Object mappingAsString) {
            this.setIndexName( name );
            this.setIndexType( type );
            this.setDocCount( count );
            this.setMapping( mappingAsString );
        }

        public String getIndexName() {
            return indexName;
        }

        public void setIndexName(String indexName) {
            this.indexName = indexName;
        }

        public long getDocCount() {
            return docCount;
        }

        public void setDocCount(long docCount) {
            this.docCount = docCount;
        }

        public Object getMapping() {
            return mapping;
        }

        public void setMapping(Object mapping) {
            this.mapping = mapping;
        }

        public String getIndexType() {
            return indexType;
        }

        public void setIndexType(String indextype) {
            this.indexType = indextype;
        }
    }
}
