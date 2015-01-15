/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.admin.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

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

public abstract class LuceneSearcher implements IConfigurable, ILuceneSearcher {

    protected IndexSearcher _indexSearcher;

    /**
     * The lock assures that no queries are made during index flipping
     */
    protected ReentrantLock searchLock = new ReentrantLock();

    private static final Log LOG = LogFactory.getLog(LuceneSearcher.class);

    public TopDocs search(final Query booleanQuery, final int start, final int length) throws Exception {

        checkLock();

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

    public Map<String, Fieldable[]> getDetails(final int docId, final String[] fieldArray) throws Exception {
        checkLock();
        // ALWAYS ADD URL FIELD TO DETAILS, no matter whether requested or not !
        // Portal decides dependent from this field how hit is rendered
        // (www-style) but does NOT
        // request the field because of bug in SE iPlug, see QueryPreProcessor
        // in Portal
        List<String> fields = new ArrayList<String>();
        Collections.addAll(fields, fieldArray);
        fields.add("url");

        final Map<String, Fieldable[]> details = new HashMap<String, Fieldable[]>();
        final Document doc = _indexSearcher.doc(docId);
        for (final String fieldName : fields) {
            // check fieldname also in lowercase if different !
            String[] fieldNamesToCheck = new String[] { fieldName };
            if (!fieldName.equals(fieldName.toLowerCase())) {
                fieldNamesToCheck = new String[] { fieldName, fieldName.toLowerCase() };
            }
            for (String fieldNameToCheck : fieldNamesToCheck) {
                final Fieldable[] values = doc.getFieldables(fieldNameToCheck);
                if (values != null && values.length > 0) {
                    details.put(fieldName, values);
                    // use first found field, do not evaluate further for
                    // lowercase field !
                    break;
                }
            }
        }
        return details;
    }

    public abstract void close() throws IOException;

    /**
     * Check if the index searcher has been locked because of index flipping.
     * 
     * @throws InterruptedException
     */
    private void checkLock() throws Exception {
        if (searchLock.isLocked()) {
            // lock search process for max 1 sec
            int cnt = 0;
            while (searchLock.isLocked() && cnt < 10) {
                Thread.sleep(100);
                cnt++;
            }
            if (cnt == 10) {
                throw new RuntimeException(
                        "Index searcher flipping in progress. Index searcher was locked for more than 1 sec.");
            }
        }
    }

    @Override
    public void configure(final PlugDescription plugDescription) {
        LOG.info("configure lucene index searcher...");
        final File workinDirectory = plugDescription.getWorkinDirectory();
        File index = new File(workinDirectory, "index");
        if (!index.exists()) {
            // flip index, just in case a new index exists
            flipIndex(plugDescription);
        }

        // check if the an index exists, this might not be
        // the case for index-less iplugs (opensearch)
        if (index.exists()) {
            try {
                if (_indexSearcher == null) {
                    LOG.info("open new index: " + index);
                    _indexSearcher = new IndexSearcher(IndexReader.open(FSDirectory.open(index), true));
                } else {
                    LOG.info("close existing index: " + index);
                    searchLock.lock();
                    close();
                    flipIndex(plugDescription);
                    LOG.info("re-open existing index: " + index);
                    _indexSearcher = new IndexSearcher(IndexReader.open(FSDirectory.open(index), true));
                }
                LOG.info("number of docs: " + _indexSearcher.maxDoc());
            } catch (final Exception e) {
                LOG.error("can not (re-)open index: " + index, e);
            } finally {
                if (searchLock.isLocked()) {
                    searchLock.unlock();
                }
            }
        } else {
            LOG.info("No index found, do not initialize IndexSearcher.");
        }
    }

    @Override
    public Document doc(int id) throws IOException {
        try {
            checkLock();
        } catch (Exception e) {
            LOG.error("Error getting document from locked index searcher for id: " + id, e);
        }
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
                LOG.warn("Unable to rename '" + newIndex.getAbsolutePath() + "' to '" + oldIndex.getAbsolutePath()
                        + "'");
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
