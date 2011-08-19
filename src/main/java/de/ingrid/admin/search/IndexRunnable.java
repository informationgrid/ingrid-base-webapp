package de.ingrid.admin.search;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexReader.FieldOption;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.ingrid.admin.object.IDocumentProducer;
import de.ingrid.admin.service.PlugDescriptionService;
import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.tool.PlugDescriptionUtil;
import de.ingrid.utils.tool.QueryUtil;

@Service
public class IndexRunnable implements Runnable, IConfigurable {

    private static final Logger LOG = Logger.getLogger(IndexRunnable.class);
    private int _documentCount;
    private IDocumentProducer _documentProducer;
    private Directory _indexDir;
    private boolean _produceable = false;
    private PlugDescription _plugDescription;
    private final IConfigurable _ingridIndexSearcher;
    private final PlugDescriptionService _plugDescriptionService;
    private String[] _dataTypes;

    @Autowired
    public IndexRunnable(@Qualifier("ingridIndexSearcher") final IConfigurable ingridIndexSearcher,
    		final PlugDescriptionService plugDescriptionService) {
        _ingridIndexSearcher = ingridIndexSearcher;
        _plugDescriptionService = plugDescriptionService;
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
                // _documentProducer.configure(_plugDescription);
                final IndexWriter writer = new IndexWriter(_indexDir,
                		new StandardAnalyzer(Version.LUCENE_CURRENT), true, IndexWriter.MaxFieldLength.LIMITED);
                while (_documentProducer.hasNext()) {
                    final Document document = _documentProducer.next();
                    for (final String dataType : _dataTypes) {
                        document.add(new Field("datatype", dataType, Store.NO, Index.NOT_ANALYZED));
                    }
                	if (_documentCount % 50 == 0) {
                		LOG.info("add document to index: " + _documentCount);
                	}
                    writer.addDocument(document);
                    _documentCount++;
                }
                LOG.info("number of produced documents: " + _documentCount);
                writer.optimize();
                writer.close();
                LOG.info("indexing ends");

                // Extend PD with all field names in index and save
                addFieldNamesToPlugdescription(_indexDir, _plugDescription);
                _plugDescriptionService.savePlugDescription(_plugDescription);

                _ingridIndexSearcher.configure(_plugDescription);
                _documentProducer.configure(_plugDescription);
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("configure plugdescription and new index dir...");        	
        }
        resetDocumentCount();
        _plugDescription = plugDescription;
        _dataTypes = plugDescription.getDataTypes();
        if (_plugDescription != null) {
            final File workinDirectory = _plugDescription.getWorkinDirectory();
            final File indexDir = new File(workinDirectory, "newIndex");
            try {
                _indexDir = FSDirectory.open(indexDir);            	
            } catch (IOException ex) {
                LOG.error("Problems creating directory for new index: " + indexDir, ex);            	
            }
        }
        // run();
    }

    public PlugDescription getPlugDescription() {
        return _plugDescription;
    }
    
    public IngridIndexSearcher getIngridIndexSearcher() {
    	return (IngridIndexSearcher) _ingridIndexSearcher;
    }

    /** Add all field names of the given index to the given plug description ! */
    public static void addFieldNamesToPlugdescription(Directory indexDir, PlugDescription pd)
    throws IOException {
    	// remove all fields
        if (LOG.isInfoEnabled()) {
            LOG.info("New Index, remove all field names from PD.");                    	
        }
    	pd.remove(PlugDescription.FIELDS);

    	// first add "metainfo" field, so plug won't be filtered when field is part of query !
        if (LOG.isInfoEnabled()) {
            LOG.info("Add meta fields to PD.");                    	
        }
    	PlugDescriptionUtil.addFieldToPlugDescription(pd, QueryUtil.FIELDNAME_METAINFO);

    	// then add fields from index
        if (LOG.isInfoEnabled()) {
            LOG.info("Add fields from new index to PD.");                    	
        }
        final IndexReader reader = IndexReader.open(indexDir, true);
        Iterator iter = reader.getFieldNames(FieldOption.ALL).iterator();
        while (iter.hasNext()) {
            String fieldName = (String)iter.next();
            pd.addField(fieldName);
            if (LOG.isDebugEnabled()) {
                LOG.debug("added index field " + fieldName + " to plugdescription.");                    	
            }
        }
        reader.close();
    }
}
