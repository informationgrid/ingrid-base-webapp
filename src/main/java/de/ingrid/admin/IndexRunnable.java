package de.ingrid.admin;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.xml.XMLSerializer;

@Service
public class IndexRunnable implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(IndexRunnable.class);
    private int _documentCount;
    private IDocumentProducer _documentProducer;
    private File _indexDir;
    private boolean _configured = false;

    @Autowired(required = false)
    public void setDocumentProducer(IDocumentProducer documentProducer) {
        _documentProducer = documentProducer;
        String property = System.getProperty("plugDescription");
        File plugDescriptionFile = new File(property);
        try {
            PlugDescription plugDescription = (PlugDescription) new XMLSerializer().deSerialize(plugDescriptionFile);
            File workinDirectory = plugDescription.getWorkinDirectory();
            _indexDir = new File(workinDirectory, "newIndex");
            _configured = true;
        } catch (IOException e) {
            LOG.warn("configuration fails. disable index creation.", e);
        }
    }

    public void run() {
        if (_configured) {
            try {
                resetDocumentCount();
                _documentProducer.initialize();
                IndexWriter writer = new IndexWriter(_indexDir, new StandardAnalyzer(), true,
                        IndexWriter.MaxFieldLength.LIMITED);
                while (_documentProducer.hasNext()) {
                    Document document = _documentProducer.next();
                    writer.addDocument(document);
                    _documentCount++;
                }
                writer.optimize();
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                resetDocumentCount();
            }
        } else {
            LOG.warn("configuration fails. disable index creation.");
        }

    }

    private void resetDocumentCount() {
        _documentCount = 0;
    }

    public int getDocumentCount() {
        return _documentCount;
    }

    public boolean isConfigured() {
        return _configured;
    }

}
