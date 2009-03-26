package de.ingrid.admin;

import java.util.Map;

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TopDocs;

public interface ILuceneSearcher {

    TopDocs search(BooleanQuery booleanQuery, int start, int length) throws Exception;

    Map<String, Fieldable[]> getDetails(int docId, String[] fields) throws Exception;
}
