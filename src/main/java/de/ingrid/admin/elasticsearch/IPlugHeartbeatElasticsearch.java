package de.ingrid.admin.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.admin.Config;
import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.object.IDocumentProducer;
import de.ingrid.elasticsearch.IBusIndexManager;
import de.ingrid.elasticsearch.IIndexManager;
import de.ingrid.elasticsearch.IndexManager;

@Service
public class IPlugHeartbeatElasticsearch extends TimerTask {
    
    private static Logger log = LogManager.getLogger(IPlugHeartbeatElasticsearch.class);

    private IIndexManager indexManager;

    private Timer timer;

    private List<String> docProducerIndices;

    @Autowired
    public IPlugHeartbeatElasticsearch(IndexManager indexManager, IBusIndexManager ibusIndexManager) {
        Config config = JettyStarter.getInstance().config;

        if (config.esRemoteNode) {
            if (config.esCommunicationThroughIBus) {
                this.indexManager = ibusIndexManager;
            } else {
                this.indexManager = indexManager;
            }
            
            int interval = config.heartbeatInterval;

            timer = new Timer( true );
            timer.schedule( this, 5000, interval * 1000 );
        }
    }

    @Autowired(required = false)
    public void setDocumentProducers(List<IDocumentProducer> documentProducers) {
        docProducerIndices = new ArrayList<String>();
        for (IDocumentProducer producer : documentProducers) {
            docProducerIndices.add( indexManager.getIndexTypeIdentifier( producer.getIndexInfo() ) );
        }
    }

    @Override
    public void run() {
        try {

            indexManager.updateHearbeatInformation( docProducerIndices );
            indexManager.flush();

        } catch (InterruptedException | ExecutionException | IOException e) {
            log.error( "Error updating Heartbeat information.", e );
        }
    }

}
