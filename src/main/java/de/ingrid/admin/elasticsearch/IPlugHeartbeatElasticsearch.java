package de.ingrid.admin.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.admin.Config;
import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.object.IDocumentProducer;

@Service
public class IPlugHeartbeatElasticsearch extends TimerTask {

    @Autowired
    private IndexManager indexManager;

    private Timer timer;

    private List<String> docProducerIndices;

    public IPlugHeartbeatElasticsearch() {
        Config config = JettyStarter.getInstance().config;

        if (config.esRemoteNode) {
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
