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
import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.PlugDescription;

public abstract class LuceneSearcher extends FlipIndex implements IConfigurable, ILuceneSearcher {

    private IndexSearcher _indexSearcher;
    private static final Log LOG = LogFactory.getLog(LuceneSearcher.class);

    public TopDocs search(final Query booleanQuery, final int start, final int length) throws Exception {
        TopDocs topDocs = _indexSearcher.search(booleanQuery, length);
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
            final Fieldable[] values = doc.getFieldables(fieldName);
            if (values != null) {
                details.put(fieldName, values);
            }
        }
        return details;
    }

    public void close() throws IOException {
        if (_indexSearcher != null) {
            _indexSearcher.close();
        }
    }

    @Override
    public void configure(final PlugDescription plugDescription) {
        super.configure(plugDescription);
        LOG.info("configure lucene index searcher...");
        final File workinDirectory = plugDescription.getWorkinDirectory();
        final File index = new File(workinDirectory, "index");
        if (index.exists()) {
            try {
                if (_indexSearcher == null) {
                    LOG.info("open new index: " + index);
                    _indexSearcher = new IndexSearcher(IndexReader.open(FSDirectory.open(index), true));
                } else {
                    LOG.info("close existing index: " + index);
                    _indexSearcher.close();
                    LOG.info("re-open existing index: " + index);
                    _indexSearcher = new IndexSearcher(IndexReader.open(FSDirectory.open(index), true));
                }
                LOG.info("number of docs: " + _indexSearcher.maxDoc());
            } catch (final Exception e) {
                LOG.error("can not (re-)open index: " + index, e);
            }
        }
    }
}
