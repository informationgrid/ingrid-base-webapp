/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;
import de.ingrid.admin.elasticsearch.IndexRunnable;
import de.ingrid.admin.elasticsearch.IndexScheduler;
import de.ingrid.utils.statusprovider.StatusProviderService;
import de.ingrid.admin.object.IDocumentProducer;
import de.ingrid.elasticsearch.ElasticConfig;
import de.ingrid.elasticsearch.IBusIndexManager;
import de.ingrid.elasticsearch.IIndexManager;
import de.ingrid.elasticsearch.IndexManager;

@Controller
public class IndexController extends AbstractController {

    @SuppressWarnings("unused")
    private static final Log LOG = LogFactory.getLog(IndexController.class);
    private IIndexManager indexManager;
    
    @Autowired(required=false)
    private List<IDocumentProducer> docProducer = new ArrayList<IDocumentProducer>();
    
    @Autowired
    private StatusProviderService statusProviderService;
    
    @Autowired
    private IndexScheduler scheduler;

    @Autowired
    public IndexController(final IndexRunnable indexRunnable, IndexManager indexManager, IBusIndexManager ibusIndexManager, ElasticConfig elasticConfig) throws Exception {
        if (elasticConfig.esCommunicationThroughIBus) {
            this.indexManager = ibusIndexManager;
            
        } else {
            this.indexManager = indexManager;
            
        }
    }

    @ModelAttribute("state")
    public Boolean injectState() {
        return scheduler.isRunning();
    }

    @RequestMapping(value = IUris.INDEXING, method = RequestMethod.GET)
    public String getIndexing(ModelMap model) {
        if (scheduler.isRunning()) {
            model.addAttribute("started", true);
        }
        return IViews.INDEXING;
    }

    @RequestMapping(value = IUris.INDEXING, method = RequestMethod.POST)
    public String postIndexing(ModelMap model) throws Exception {
        scheduler.triggerManually();
        model.addAttribute("started", true);
        
        return IViews.INDEXING;
    }
    
    @RequestMapping(value = IUris.INDEX_STATE, method = RequestMethod.GET)
    public String getIndexState(ModelMap model){
    	model.addAttribute("state", scheduler.isRunning());
    	return "/base/indexState";
    }
    
    @RequestMapping(value = IUris.INDEX_STATUS, method = RequestMethod.GET)
    public String getIndexStatus(final ModelMap modelMap) throws Exception {
        // get cluster health information
        //ClusterHealthResponse clusterHealthResponse = indexManager.getClient().admin().cluster().health( new ClusterHealthRequest() ).get();
// TODO: implement
        // get all indices
//        List<IndexStatus> indices = new ArrayList<IndexStatus>();
//        for (IDocumentProducer producer : docProducer) {
//            
//            IndexInfo indexInfo = Utils.getIndexInfo( producer, baseConfig );
//            String indexAlias = indexInfo.getToAlias();
//            String index = indexInfo.getToIndex();
//            String indexType = indexInfo.getToType();
//            
//            String currentIndex = indexManager.getIndexNameFromAliasName(indexAlias, index);
//            if (currentIndex == null) continue;
//    
//            // get mapping
//            GetMappingsResponse mappingResponse = indexManager.getClient().admin().indices().getMappings( new GetMappingsRequest() ).get();
//            ImmutableOpenMap<String, MappingMetaData> mapping = mappingResponse.getMappings().get( currentIndex );
//            
//            long count = indexManager.getClient().prepareSearch( currentIndex ).setTypes( indexType ).setSource(new SearchSourceBuilder().size(0)).get().getHits().totalHits;
//
//            Object mappingAsString = mapping.get( indexType ) != null ? mapping.get( indexType ).source() : "\"No mapping exists!\"";
//            IndexStatus indexStatus = new IndexStatus( currentIndex, indexType, count, mappingAsString );
//            indices.add( indexStatus );
//        }
//        modelMap.addAttribute( "clusterState", clusterHealthResponse );
//        modelMap.addAttribute( "indices", indices );
        return IViews.INDEX_STATUS;
    }

    @RequestMapping(value = IUris.INDEX_STATUS, method = RequestMethod.POST)
    public String setIndexStatus(@RequestParam("action") final String action) throws IOException {
        return redirect( IUris.INDEX_STATUS );
    }
    
    @RequestMapping(value = IUris.LIVE_INDEX_STATE, method = RequestMethod.GET)
    public @ResponseBody
    StatusResponse getLiveIndexState(ModelMap model) {
        return new StatusResponse(scheduler.isRunning(), statusProviderService.getDefaultStatusProvider().toString());
    }
    
    private class StatusResponse {

        private Boolean isRunning;
        private String status;

        public StatusResponse(Boolean isRunning, String status) {
            this.setIsRunning(isRunning);
            this.setStatus(status);
        }

        public void setStatus(String status) {
            this.status = status;
        }

        @SuppressWarnings("unused")
        public String getStatus() {
            return status;
        }

        public void setIsRunning(Boolean isRunning) {
            this.isRunning = isRunning;
        }

        @SuppressWarnings("unused")
        public Boolean getIsRunning() {
            return isRunning;
        }

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
