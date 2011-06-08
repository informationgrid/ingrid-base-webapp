package de.ingrid.admin.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.IDetailer;
import de.ingrid.utils.IRecordLoader;
import de.ingrid.utils.ISearcher;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.dsc.Column;
import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.tool.QueryUtil;

@Service
@Qualifier("ingridIndexSearcher")
public class IngridIndexSearcher extends LuceneSearcher implements ISearcher, IDetailer, IRecordLoader, IConfigurable {

    private String _plugId;
    private static final Log LOG = LogFactory.getLog(IngridIndexSearcher.class);
    private final QueryParsers _queryParsers;

    // NOTICE:
    // We use autowiring for "QueryParsers" instance BUT DEFINE THE
    // "QueryParsers" BEAN IN XML with a qualifier !
    // The bean is created by the factory, then autowiring takes place and the
    // bean is injected here !
    // This way we can set the order and the instances of the parsers in XML !
    // The "XMLconfigured" qualifier identifies the instance defined in XML !
    @Autowired
    public IngridIndexSearcher(@Qualifier("XMLconfigured") QueryParsers queryParsers) {
        _queryParsers = queryParsers;
    }

    public IngridHits search(IngridQuery ingridQuery, int start, int length) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("incoming query: " + ingridQuery);
            LOG.debug("start: " + start);
            LOG.debug("length: " + length);
        }

    	// remove "meta" field from query so search works !
    	QueryUtil.removeFieldFromQuery(ingridQuery, QueryUtil.FIELDNAME_METAINFO);

        Query luceneQuery = _queryParsers.parse(ingridQuery);

        if (LOG.isDebugEnabled()) {
            LOG.debug("outgoing lucene query: " + luceneQuery);
            explainQuery(luceneQuery);
        }

        TopDocs topDocs = search(luceneQuery, start, length);
        LOG.debug("found hits: " + topDocs.scoreDocs.length + "/" + topDocs.totalHits);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        IngridHit[] ingridHitArray = new IngridHit[scoreDocs.length];
        for (int i = 0; i < scoreDocs.length; i++) {
            ScoreDoc scoreDoc = scoreDocs[i];
            int docid = scoreDoc.doc;
            float score = scoreDoc.score;
            IngridHit ingridHit = new IngridHit(_plugId, docid, -1, score);
            ingridHitArray[i] = ingridHit;
        }
        IngridHits ingridHits = new IngridHits(_plugId, topDocs.totalHits, ingridHitArray, true);
        return ingridHits;
    }

    public IngridHitDetail getDetail(IngridHit ingridHit, IngridQuery ingridQuery, String[] fields) throws Exception {
        int docId = ingridHit.getDocumentId();

        Map<String, Fieldable[]> details = getDetails(docId, new String[] { "title", "summary" });
        String title = getValue("title", details);
        String summary = getValue("summary", details);

        IngridHitDetail ingridHitDetail = new IngridHitDetail(ingridHit, title, summary);
        details = getDetails(docId, fields);
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
        if (fieldables != null) {
            for (Fieldable fieldable : fieldables) {
                value += fieldable.stringValue() + " ";
            }
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

    @Override
    public void configure(PlugDescription plugDescription) {
        super.configure(plugDescription);
        LOG.info("configure plug id...");
        _plugId = plugDescription.getPlugId();
    }

    @Override
    public Record getRecord(IngridHit hit) throws Exception {
        int documentId = hit.getDocumentId();
        Document document = doc(documentId);
        List fields = document.getFields();
        Record record = new Record();
        for (Object object : fields) {
            Field field = (Field) object;
            String name = field.name();
            String stringValue = field.stringValue();
            Column column = new Column(null, name, null, true);
            column.setTargetName(name);
            record.addColumn(column, stringValue);
        }
        return record;
    }

    private void explainQuery(Query query) {
        if (query instanceof BooleanQuery) {
            BooleanClause[] clauses = ((BooleanQuery) query).getClauses();
            LOG.debug("BQ  { ");
            for (int i = 0; i < clauses.length; i++) {
                LOG.debug("Clause (" + clauses[i].getOccur() + ")  { ");
                explainQuery(clauses[i].getQuery());
                LOG.debug("} ");
            }
            LOG.debug("} ");
        } else if (query instanceof PhraseQuery) {
            Term[] terms = ((PhraseQuery) query).getTerms();
            LOG.debug("PQ { ");
            for (int i = 0; i < terms.length; i++) {
                LOG.debug("Term (" + terms[i].field() + " : " + terms[i].text() + ")");
            }
            LOG.debug("} ");
        } else if (query instanceof NumericRangeQuery) {
            NumericRangeQuery nrfq = ((NumericRangeQuery) query);
            LOG.debug("NRQ { ");
            LOG.debug(nrfq.getField() + "( min:" + nrfq.getMin() + " - max:" + nrfq.getMax() + ")");
            LOG.debug("} ");
        } else {
            LOG.debug("unkown query type: " + query.getClass().getName());
        }
    }
}
