/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.admin.elasticsearch;

import de.ingrid.admin.Config;
import de.ingrid.admin.Utils;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.utils.statusprovider.StatusProvider;
import de.ingrid.utils.statusprovider.StatusProvider.Classification;
import de.ingrid.utils.statusprovider.StatusProviderService;
import de.ingrid.admin.object.IDocumentProducer;
import de.ingrid.admin.service.PlugDescriptionService;
import de.ingrid.elasticsearch.*;
import de.ingrid.iplug.IPlugdescriptionFieldFilter;
import de.ingrid.iplug.PlugDescriptionFieldFilters;
import de.ingrid.utils.ElasticDocument;
import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.tool.PlugDescriptionUtil;
import de.ingrid.utils.tool.QueryUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.common.Strings;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static de.ingrid.utils.PlugDescription.QUERY_EXTENSION_CONTAINER;

@Service
public class IndexRunnable implements Runnable, IConfigurable {

    private static final Logger LOG = Logger.getLogger(IndexRunnable.class);
    private PlugDescriptionFieldFilters plugDescriptionFieldFilters;
    private List<IDocumentProducer> _documentProducers;
    private boolean _produceable = false;
    private PlugDescription _plugDescription;
    private final PlugDescriptionService _plugDescriptionService;
    private final IIndexManager _indexManager;

    private final ConcurrentMap<String, Object> _indexHelper;

    private StatusProvider statusProvider;
    
    private final Config config;

    private final ElasticConfig elasticConfig;

    /**
     * @param pds              is the service to handle PlugDescriptions
     * @param indexManager     is the manager to handle indices directly
     * @param ibusIndexManager is the manager to handle indices via the iBus
     */
    @Autowired
    public IndexRunnable(PlugDescriptionService pds, IndexManager indexManager, IBusIndexManager ibusIndexManager, Config config, ElasticConfig elasticConfig, Optional<IPlugdescriptionFieldFilter[]> fieldFilters, StatusProviderService statusProviderService) {
        // Config config = config;
        this.config = config;
        this.elasticConfig = elasticConfig;
        this.statusProvider = statusProviderService.getDefaultStatusProvider();

        _plugDescriptionService = pds;
        try {
            _plugDescription = pds.getPlugDescription();
        } catch (IOException e) {
            LOG.error("Error getting PlugDescription from service", e);
        }

        this.plugDescriptionFieldFilters = fieldFilters
                .map(PlugDescriptionFieldFilters::new)
                .orElseGet(() -> new PlugDescriptionFieldFilters(new IPlugdescriptionFieldFilter[0]));

        _indexManager = elasticConfig.esCommunicationThroughIBus ? ibusIndexManager : indexManager;

        LOG.info("Communication to Elasticsearch is " + (elasticConfig.esCommunicationThroughIBus ? "through iBus" : "direct"));

        _indexHelper = new ConcurrentHashMap<>();
    }

    @Autowired(required = false)
    public void setDocumentProducers(List<IDocumentProducer> documentProducers) {
        _documentProducers = documentProducers;
        _produceable = true;

        elasticConfig.activeIndices = getIndexNamesFromProducers(documentProducers);
    }

    private IndexInfo[] getIndexNamesFromProducers(List<IDocumentProducer> documentProducers) {
        List<IndexInfo> indices = new ArrayList<>();

        for (IDocumentProducer docProducer : documentProducers) {
            IndexInfo indexInfo = Utils.getIndexInfo(docProducer, config);
            indices.add(indexInfo);
        }
        return indices.toArray(new IndexInfo[0]);
    }

    @Override
    public void run() {
        if (_produceable) {
            // remember newIndex in case it has to be cleaned up, after an unsuccessful index process
            String newIndex = null;
            try {
                statusProvider.clear();
                statusProvider.addState("start_indexing", "Start indexing");
                LOG.info("indexing starts");

                // remove all fields from plug description
                if (LOG.isInfoEnabled()) {
                    LOG.info("New Index, remove all field names from PD.");
                }
                _plugDescription.remove(PlugDescription.FIELDS);

                int documentCount = 0;
                String oldIndex = null;
                Map<String, String[]> indexNames = new HashMap<>();

                // check if pluginfo index exists or create it
                this._indexManager.checkAndCreateInformationIndex();

                for (IDocumentProducer producer : _documentProducers) {
                    _indexHelper.clear();
                    IndexInfo info = Utils.getIndexInfo(producer, config);
                    // get the current index from the alias name
                    // if it's the first time then use the name given by the
                    // configuration
                    // only create new index if we did not already ... this depends on the producer settings
                    if (!indexNames.containsKey(info.getToIndex())) {
                        // filter by iPlug UUID to correctly identify index
                        oldIndex = _indexManager.getIndexNameFromAliasName(info.getToAlias(), config.uuid);
                        String strippedComponentName = config.communicationProxyUrl.replaceAll("[^a-zA-Z-]","");
                        newIndex = IndexManager.getNextIndexName(oldIndex == null ? info.getToIndex() : oldIndex, config.uuid, strippedComponentName);
                        if (config.alwaysCreateNewIndex) {
                            String mapping = _indexManager.getDefaultMapping();
                            String settings = _indexManager.getDefaultSettings();
                            if (mapping != null) {
                                _indexManager.createIndex(newIndex, info.getToType(), mapping, settings);
                            } else {
                                this.statusProvider.addState("MAPPING_ERROR", "Could not get default mapping to create index", Classification.WARN);
                                _indexManager.createIndex(newIndex);
                            }
                        }
                        indexNames.put(info.getToIndex(), new String[]{oldIndex, newIndex, info.getToAlias()});
                    }

                    // set name of new (or old) index
                    info.setRealIndexName(config.alwaysCreateNewIndex ? newIndex : oldIndex);

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
                    String plugIdInfo = _indexManager.getIndexTypeIdentifier(info);

                    while (producer.hasNext()) {

                        final ElasticDocument document = producer.next();
                        if (document == null) {
                            LOG.warn("DocumentProducer " + producer + " returned null Document, we skip this record (not added to index)!");
                            this.statusProvider.addState(indexTag + "_skipped", "Skipped documents: " + (++skip), Classification.WARN);
                            continue;
                        }

                        // add partner, provider and datatypes
                        addBasicFields(document, info);

                        this.statusProvider.addState(indexTag, "Indexing document: " + (count++) + indexPostfixString);

                        // add document to index
                        _indexManager.update(info, document, false);

                        // send info every 100 docs
                        if (count % 100 == 2) {
                            this._indexManager.updateIPlugInformation(plugIdInfo, getIPlugInfo(plugIdInfo, info, oldIndex, true, count - 1, totalCount));
                        }

                        collectIndexFields(document);

                        documentCount++;
                    }

                    if (documentCount > 0) {
                        writeFieldNamesToPlugdescription();
                    }
                    
                    // update central index with iPlug information
                    this._indexManager.updateIPlugInformation(plugIdInfo, getIPlugInfo(plugIdInfo, info, newIndex, false, null, null));

                    // update index now!
                    _indexManager.flush();

                    producer.configure(_plugDescription);
                }

                LOG.info("number of produced documents: " + documentCount);

                LOG.info("indexing ends");

                if (config.alwaysCreateNewIndex) {
                    switchIndexAlias(oldIndex, indexNames);
                }
                /* else {
                    // TODO: remove documents which have not been updated (hence removed!)
                }*/

                this.statusProvider.addState("stop_indexing", "Indexing finished.");

                // update new fields into override property
                PlugdescriptionCommandObject pdObject = new PlugdescriptionCommandObject();
                pdObject.putAll(_plugDescription);
                config.writePlugdescriptionToProperties(pdObject);

                _plugDescriptionService.savePlugDescription(_plugDescription);

            } catch (final Exception e) {
                this.statusProvider.addState("error_indexing", "An exception occurred: " + e.getMessage(), Classification.ERROR);
                LOG.error("Exception occurred during indexing: ", e);
                cleanUp(newIndex);
            } catch (Throwable t) {
                this.statusProvider.addState("error_indexing", "An exception occurred: " + t.getMessage() + ". Try increasing the HEAP-size or let it manage automatically.", Classification.ERROR);
                LOG.error("Error during indexing", t);
                LOG.info("Try increasing the HEAP-size or let it manage automatically.");
                cleanUp(newIndex);
            } finally {
                try {
                    this.statusProvider.write();
                } catch (IOException e) {
                    LOG.error("Could not write status provider file", e);
                }
            }
        } else {
            LOG.warn("configuration fails. disable index creation.");
        }

    }

    private void switchIndexAlias(String oldIndex, Map<String, String[]> indexNames) {
        // switch aliases of all document producers to the new indices
        for (String index : indexNames.keySet()) {
            String[] indexMore = indexNames.get(index);
            String newIndex = indexMore[1];
            _indexManager.switchAlias(indexMore[2], indexMore[0], newIndex);
            if (oldIndex != null) {
                removeOldIndices(newIndex);
            }
            this.statusProvider.addState("switch_index", "Switch to newly created index: " + newIndex + " under the alias: " + indexMore[2]);
        }
        LOG.info("switched alias to new index and deleted old one");
    }

    private void removeOldIndices(String newIndex) {
        int delimiterPos = newIndex.lastIndexOf("_");
        String indexGroup = newIndex.substring(0, delimiterPos + 1);
        String[] indices = _indexManager.getIndices(indexGroup);
        if (indices != null) {
            for (String indexToDelete : indices) {
                if (!indexToDelete.equals(newIndex)) {
                    _indexManager.deleteIndex(indexToDelete);
                }
            }
        } else {
            LOG.warn("No indices found with prefix: " + indexGroup + " which we wanted to clean up after indexing");
        }
    }

    private String getIPlugInfo(String infoId, IndexInfo info, String indexName, boolean running, Integer count, Integer totalCount) throws IOException {
        Config _config = config;

        // @formatter:off
        XContentBuilder xContentBuilder = XContentFactory.jsonBuilder().startObject()
                .field("plugId", _config.communicationProxyUrl)
                .field("indexId", infoId)
                .field("iPlugName", _config.datasourceName)
                .field("linkedIndex", indexName)
                .field("linkedType", info.getToType())
                .field("adminUrl", _config.guiUrl)
                .field("lastHeartbeat", new Date())
                .field("lastIndexed", new Date())
                .field("plugdescription", this._plugDescription)
                .startObject("indexingState")
                    .field("numProcessed", count)
                    .field("totalDocs", totalCount)
                    .field("running", running)
                    .endObject()
                .endObject();
        // @formatter:on

        return Strings.toString(xContentBuilder);
    }

    private void cleanUp(String newIndex) {
        if (config.alwaysCreateNewIndex && newIndex != null) {
            _indexManager.deleteIndex(newIndex);
        }
        statusProvider.addState("CLEANUP", "Cleaned up data and reverted to old index");
    }

    public boolean isProduceable() {
        return _produceable;
    }

    @Override
    public void configure(final PlugDescription plugDescription) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("configure plugdescription and new index dir...");
        }

        _plugDescription = plugDescriptionFieldFilters.filter(plugDescription);
        _plugDescription.remove(QUERY_EXTENSION_CONTAINER);
    }

    public PlugDescription getPlugDescription() {
        return _plugDescription;
    }

    private void collectIndexFields(ElasticDocument ed) {
        for (Entry<String, Object> entry : ed.entrySet()) {
            String key = entry.getKey();
            if (key == null) {
                LOG.warn("A key of an ElasticDocument was null, when collecting fields for PlugDescription");
            } else {
                _indexHelper.putIfAbsent(key, "");
            }
        }
    }

    private void writeFieldNamesToPlugdescription() {
        // first add "metainfo" field, so plug won't be filtered when field is
        // part of query !
        if (LOG.isInfoEnabled()) {
            LOG.info("Add meta fields to PD.");
        }
        PlugDescriptionUtil.addFieldToPlugDescription(_plugDescription, QueryUtil.FIELDNAME_METAINFO);
        PlugDescriptionUtil.addFieldToPlugDescription(_plugDescription, QueryUtil.FIELDNAME_INCL_META);

        // then add fields from index
        if (LOG.isInfoEnabled()) {
            LOG.info("Add fields from new index to PD.");
        }
        for (String property : _indexHelper.keySet()) {
            _plugDescription.addField(property);
            LOG.debug(String.format("added index field %s to plugdescription.", property));
        }
    }

    public void setStatusProvider(StatusProvider statusProvider) {
        this.statusProvider = statusProvider;
    }


    private void addBasicFields(ElasticDocument document, IndexInfo info) {
        String[] datatypes = null;
        try {
            String datatypesString = (String) config.getOverrideProperties().get( "plugdescription.dataType." + info.getIdentifier()  );
            if (datatypesString != null) {
                datatypes = datatypesString.split(",");
            }
        } catch (IOException e) {
            LOG.error("Could not get override properties", e);
        }

        if (datatypes == null) {
            datatypes = config.datatypes.toArray(new String[0]);
        }

        for (String datatype : datatypes) {
            document.put("datatype", datatype);
        }
        document.put(PlugDescription.PARTNER, config.partner);
        document.put(PlugDescription.PROVIDER, config.provider);
        document.put(PlugDescription.DATA_SOURCE_NAME, config.datasourceName);
        document.put(PlugDescription.ORGANISATION, config.organisation);
        document.put("iPlugId", config.communicationProxyUrl);

        String sortString = Arrays.stream(document.getValues("title")).collect(Collectors.joining());
        sortString += Arrays.stream(document.getValues("url")).collect(Collectors.joining());
        document.put("sort_hash", DigestUtils.shaHex(sortString));
    }
}
