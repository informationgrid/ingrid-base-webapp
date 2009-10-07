package de.ingrid.admin.service;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.ingrid.admin.object.IDocumentProducer;
import de.ingrid.admin.object.INewIndexListener;
import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.PlugDescription;

@Service
public class IndexRunnable implements Runnable, IConfigurable {

    private static final Log LOG = LogFactory.getLog(IndexRunnable.class);
    private int _documentCount;
    private IDocumentProducer _documentProducer;
    private File _indexDir;
    private boolean _produceable = false;
    private final INewIndexListener _indexClosedListener;
    private PlugDescription _plugDescription;

    @Autowired
    public IndexRunnable(@Qualifier("flipIndex") final INewIndexListener indexClosedListener) {
        _indexClosedListener = indexClosedListener;
    }

    @Autowired(required = false)
    public void setDocumentProducer(final IDocumentProducer documentProducer) {
        _documentProducer = documentProducer;
        _produceable = true;
    }

    public void run() {
        if (_produceable) {
            try {
                LOG.info("indexing starts");
                resetDocumentCount();
                _documentProducer.initialize();
                final IndexWriter writer = new IndexWriter(_indexDir, new StandardAnalyzer(), true,
                        IndexWriter.MaxFieldLength.LIMITED);
                while (_documentProducer.hasNext()) {
                    final Document document = _documentProducer.next();
                    LOG.debug("add document to index: " + _documentCount);
                    writer.addDocument(document);
                    _documentCount++;
                }
                writer.optimize();
                writer.close();
                LOG.info("indexing ends");
                _indexClosedListener.indexIsCreated();
            } catch (final Exception e) {
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

    public boolean isProduceable() {
        return _produceable;
    }

    public void configure(final PlugDescription plugDescription) {
        LOG.debug("reconfigure...");
        _plugDescription = plugDescription;
        if (_plugDescription != null) {
            final File workinDirectory = _plugDescription.getWorkinDirectory();
            _indexDir = new File(workinDirectory, "newIndex");
        }
    }

    public PlugDescription getPlugDescription() {
        return _plugDescription;
    }
}
