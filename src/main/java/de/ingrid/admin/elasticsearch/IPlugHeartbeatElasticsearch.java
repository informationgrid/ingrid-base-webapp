package de.ingrid.admin.elasticsearch;

import de.ingrid.admin.Config;
import de.ingrid.admin.object.IDocumentProducer;
import de.ingrid.elasticsearch.ElasticConfig;
import de.ingrid.elasticsearch.IBusIndexManager;
import de.ingrid.elasticsearch.IIndexManager;
import de.ingrid.elasticsearch.IndexManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
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

        timer = new Timer( true );
        timer.schedule( this, 5000, interval * 1000L );
    }

    @Autowired(required = false)
    public void setDocumentProducers(List<IDocumentProducer> documentProducers) {
        docProducerIndices = new ArrayList<>();
        for (IDocumentProducer producer : documentProducers) {
            docProducerIndices.add( indexManager.getIndexTypeIdentifier( producer.getIndexInfo() ) );
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
            log.error( "Error updating Heartbeat information.", e );
        }
    }
    
    private Map<String, String> getIPlugInfos(List<String> docProducerIndices) {
		Map<String,String> map = new HashMap<>();
		
		for (String docProdId : docProducerIndices) {
			map.put(docProdId, getHearbeatInfo(docProdId));
		}
		
		return map;
	}

	private String getHearbeatInfo(String id) {
		try {
            XContentBuilder xContentBuilder = XContentFactory.jsonBuilder().startObject()
                    .field("plugId", config.communicationProxyUrl)
                    .field("indexId", id)
                    .field("lastHeartbeat", new Date())
                    .endObject();
            return Strings.toString(xContentBuilder);
        } catch (IOException ex) {
			log.error("Error creating iPlug information", ex);
			return null;
		}
    }

}