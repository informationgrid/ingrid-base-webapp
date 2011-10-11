package de.ingrid.admin.search;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import de.ingrid.admin.object.ILuceneSearcher;
import de.ingrid.facetsearch.utils.LuceneIndexReaderWrapper;
import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.PlugDescription;

public abstract class LuceneSearcher implements IConfigurable, ILuceneSearcher {

    protected IndexSearcher _indexSearcher;
    
    private static final Log LOG = LogFactory.getLog(LuceneSearcher.class);

    public TopDocs search(final Query booleanQuery, final int start, final int length) throws Exception {
    	// determine max num to fetch
    	int maxNumDocs = _indexSearcher.maxDoc();
    	if (maxNumDocs < length) {
    		maxNumDocs = length;
    	}
        TopDocs topDocs = _indexSearcher.search(booleanQuery, maxNumDocs);
        final ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        int size = 0;
        final int lengthMinusStart = scoreDocs.length - start;
        if (lengthMinusStart >= 0) {
            size = Math.min(length, lengthMinusStart);
        }
        final ScoreDoc[] pagedScoreDocs = new ScoreDoc[size];
        System.arraycopy(scoreDocs, start, pagedScoreDocs, 0, size);
        float maxScore = -1;
        for (final ScoreDoc scoreDoc : pagedScoreDocs) {
            final float score = scoreDoc.score;
            maxScore = maxScore < score ? score : maxScore;
        }
        topDocs = new TopDocs(topDocs.totalHits, pagedScoreDocs, maxScore);
        return topDocs;
    }

    public Map<String, Fieldable[]> getDetails(final int docId, final String[] fields) throws Exception {
        final Map<String, Fieldable[]> details = new HashMap<String, Fieldable[]>();
        final Document doc = _indexSearcher.doc(docId);
        for (final String fieldName : fields) {
        	// check fieldname also in lowercase if different !
        	String[] fieldNamesToCheck = new String[]{ fieldName };
        	if (!fieldName.equals(fieldName.toLowerCase())) {
        		fieldNamesToCheck = new String[]{ fieldName, fieldName.toLowerCase() };
        	}
        	for (String fieldNameToCheck : fieldNamesToCheck) {
                final Fieldable[] values = doc.getFieldables(fieldNameToCheck);
                if (values != null && values.length > 0) {
                    details.put(fieldName, values);
                    // use first found field, do not evaluate further for lowercase field !
                    break;
                }
        	}
        }
        return details;
    }

    public abstract void close() throws IOException;

    @Override
    public void configure(final PlugDescription plugDescription) {
        LOG.info("configure lucene index searcher...");
        final File workinDirectory = plugDescription.getWorkinDirectory();
        final File index = new File(workinDirectory, "index");
        if (!index.exists()) {
        	flipIndex(plugDescription);
        }
        try {
            if (_indexSearcher == null) {
                LOG.info("open new index: " + index);
                _indexSearcher = new IndexSearcher(IndexReader.open(FSDirectory.open(index), true));
            } else {
                LOG.info("close existing index: " + index);
                close();
                flipIndex(plugDescription);
                LOG.info("re-open existing index: " + index);
                _indexSearcher = new IndexSearcher(IndexReader.open(FSDirectory.open(index), true));
            }
            LOG.info("number of docs: " + _indexSearcher.maxDoc());
        } catch (final Exception e) {
            LOG.error("can not (re-)open index: " + index, e);
        }
    }

    @Override
    public Document doc(int id) throws IOException {
        return _indexSearcher.doc(id);
    }
    
    private void flipIndex(PlugDescription plugDescription) {
        File workinDirectory = plugDescription.getWorkinDirectory();
        File oldIndex = new File(workinDirectory, "index");
        File newIndex = new File(workinDirectory, "newIndex");
        if (newIndex.exists()) {
            LOG.info("delete index: " + oldIndex);
            delete(oldIndex);
            LOG.info("rename index: " + newIndex);
            if (!newIndex.renameTo(oldIndex)) {
                LOG.warn("Unable to rename '" + newIndex.getAbsolutePath() + "' to '" + oldIndex.getAbsolutePath() + "'");
            }
        }
    }

    private void delete(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isDirectory()) {
                    delete(file);
                }
                if (!file.delete()) {
                    LOG.warn("Unable to delete file: " + file.getAbsolutePath());
                }
            }
        }
        if (folder.exists() && !folder.delete()) {
            LOG.warn("Unable to delete folder: " + folder.getAbsolutePath());
        }
    }    
}
