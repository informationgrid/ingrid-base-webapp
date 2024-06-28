/*-
 * **************************************************-
 * InGrid Base-Webapp
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

import com.fasterxml.jackson.databind.util.StdDateFormat;
import de.ingrid.admin.Config;
import de.ingrid.admin.object.IDocumentProducer;
import de.ingrid.elasticsearch.ElasticConfig;
import de.ingrid.elasticsearch.IBusIndexManager;
import de.ingrid.elasticsearch.IIndexManager;
import de.ingrid.elasticsearch.IndexManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class IPlugHeartbeatElasticsearch extends TimerTask {

    private static Logger log = LogManager.getLogger(IPlugHeartbeatElasticsearch.class);

    private IIndexManager indexManager;

    private Timer timer;

    private List<String> docProducerIndices;

    private final Config config;

    @Autowired
    public IPlugHeartbeatElasticsearch(IndexManager indexManager, IBusIndexManager ibusIndexManager, Config config, ElasticConfig elasticConfig) {
        this.config = config;

        if (elasticConfig.esCommunicationThroughIBus) {
            this.indexManager = ibusIndexManager;
        } else {
            this.indexManager = indexManager;
        }

        int interval = config.heartbeatInterval;

        timer = new Timer(true);
        timer.schedule(this, 5000, interval * 1000L);
    }

    @Autowired(required = false)
    public void setDocumentProducers(List<IDocumentProducer> documentProducers) {
        docProducerIndices = new ArrayList<>();
        for (IDocumentProducer producer : documentProducers) {
            docProducerIndices.add(indexManager.getIndexTypeIdentifier(producer.getIndexInfo()));
        }
    }

    @Override
    public void run() {
        try {
            if (docProducerIndices == null) {
                log.error("No index info for this iPlug defined!");
            } else {
                indexManager.updateHearbeatInformation(getIPlugInfos(docProducerIndices));
                indexManager.flush();
            }

        } catch (InterruptedException | ExecutionException | IOException e) {
            log.error("Error updating Heartbeat information.", e);
        }
    }

    private Map<String, Object> getIPlugInfos(List<String> docProducerIndices) {
        Map<String, Object> map = new HashMap<>();

        for (String docProdId : docProducerIndices) {
            map.put(docProdId, getHearbeatInfo(docProdId));
        }

        return map;
    }

    private JSONObject getHearbeatInfo(String id) {
        JSONObject json = new JSONObject();
        json.put("plugId", config.communicationProxyUrl);
        json.put("indexId", id);
        json.put("lastHeartbeat", new StdDateFormat().format(new Date()));
        return json;
    }

}
