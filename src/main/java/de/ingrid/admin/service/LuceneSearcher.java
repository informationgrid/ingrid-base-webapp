package de.ingrid.admin.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import de.ingrid.admin.object.ILuceneSearcher;
import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.PlugDescription;

@Service
public class LuceneSearcher implements IConfigurable, ILuceneSearcher {

    private IndexSearcher _indexSearcher;
    private static final Log LOG = LogFactory.getLog(LuceneSearcher.class);

    public TopDocs search(BooleanQuery booleanQuery, int start, int length) throws Exception {
        TopDocs topDocs = _indexSearcher.search(booleanQuery, length);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        int size = 0;
        final int lengthMinusStart = scoreDocs.length - start;
        if (lengthMinusStart >= 0) {
            size = Math.min(length, lengthMinusStart);
        }
        ScoreDoc[] pagedScoreDocs = new ScoreDoc[size];
        System.arraycopy(scoreDocs, start, pagedScoreDocs, 0, size);
        float maxScore = -1;
        for (ScoreDoc scoreDoc : pagedScoreDocs) {
            float score = scoreDoc.score;
            maxScore = maxScore < score ? score : maxScore;
        }
        topDocs = new TopDocs(topDocs.totalHits, pagedScoreDocs, maxScore);
        return topDocs;
    }

    public Map<String, Fieldable[]> getDetails(int docId, String[] fields) throws Exception {
        Map<String, Fieldable[]> details = new HashMap<String, Fieldable[]>();
        Document doc = _indexSearcher.doc(docId);
        for (String fieldName : fields) {
            Fieldable[] values = doc.getFieldables(fieldName);
            if (values != null) {
                details.put(fieldName, values);
            }
        }
        return details;
    }

    public void close() throws IOException {
        _indexSearcher.close();
    }

    public void configure(PlugDescription plugDescription) {
        LOG.debug("reconfigure...");
        File workinDirectory = plugDescription.getWorkinDirectory();
        File index = new File(workinDirectory, "index");
        try {
            _indexSearcher = new IndexSearcher(FSDirectory.getDirectory(index));
            // TODO throw exception?
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
