package de.ingrid.admin;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.admin.query.IQueryParser;
import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.IDetailer;
import de.ingrid.utils.ISearcher;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.query.IngridQuery;

@Service
public class IngridIndexSearcher implements ISearcher, IDetailer, IConfigurable {

    private final ILuceneSearcher _luceneSearcher;
    private String _plugId;
    private final List<IQueryParser> _queryParsers;

    @Autowired
    public IngridIndexSearcher(ILuceneSearcher luceneSearcher, List<IQueryParser> queryParsers) {
        _luceneSearcher = luceneSearcher;
        _queryParsers = queryParsers;
    }

    public void configure(PlugDescription plugDescription) {
        _plugId = plugDescription.getPlugId();
        if (_luceneSearcher instanceof IConfigurable) {
            ((IConfigurable) _luceneSearcher).configure(plugDescription);
        }
    }

    public IngridHits search(IngridQuery ingridQuery, int start, int length) throws Exception {
        BooleanQuery booleanQuery = new BooleanQuery();
        for (IQueryParser queryParser : _queryParsers) {
            queryParser.parse(ingridQuery, booleanQuery);
        }
        TopDocs topDocs = _luceneSearcher.search(booleanQuery, start, length);
        IngridHits ingridHits = new IngridHits();
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        IngridHit[] ingridHitArray = new IngridHit[scoreDocs.length];
        for (int i = 0; i < scoreDocs.length; i++) {
            ScoreDoc scoreDoc = scoreDocs[i];
            int docid = scoreDoc.doc;
            float score = scoreDoc.score;
            IngridHit ingridHit = new IngridHit(_plugId, docid, -1, score);
            ingridHitArray[i] = ingridHit;
        }
        ingridHits = new IngridHits(_plugId, topDocs.totalHits, ingridHitArray, true);
        return ingridHits;
    }

    public void close() throws Exception {
        // TODO Auto-generated method stub
    }

    public IngridHitDetail getDetail(IngridHit ingridHit, IngridQuery ingridQuery, String[] fields) throws Exception {
        int docId = ingridHit.getDocumentId();

        Map<String, Fieldable[]> details = _luceneSearcher.getDetails(docId, new String[] { "title", "summary" });
        String title = getValue("title", details);
        String summary = getValue("summary", details);

        IngridHitDetail ingridHitDetail = new IngridHitDetail(ingridHit, title, summary);
        details = _luceneSearcher.getDetails(docId, fields);
        Set<String> keySet = details.keySet();
        for (String field : keySet) {
            String[] values = getValues(field, details);
            ingridHitDetail.put(field, values);
        }
        return ingridHitDetail;
    }

    private String[] getValues(String field, Map<String, Fieldable[]> details) {
        Fieldable[] fieldables = details.get(field);
        String[] values = new String[fieldables.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = fieldables[i].stringValue();
        }
        return values;
    }

    private String getValue(String key, Map<String, Fieldable[]> titleAndSummary) {
        String value = "";
        Fieldable[] fieldables = titleAndSummary.get(key);
        for (Fieldable fieldable : fieldables) {
            value += fieldable.stringValue() + " ";
        }
        return value;
    }

    public IngridHitDetail[] getDetails(IngridHit[] ingridHits, IngridQuery ingridQuery, String[] fields)
            throws Exception {
        IngridHitDetail[] details = new IngridHitDetail[ingridHits.length];
        for (int i = 0; i < ingridHits.length; i++) {
            IngridHit ingridHit = ingridHits[i];
            IngridHitDetail detail = getDetail(ingridHit, ingridQuery, fields);
            details[i] = detail;
        }
        return details;
    }

}
