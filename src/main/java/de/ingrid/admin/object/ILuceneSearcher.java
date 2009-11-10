package de.ingrid.admin.object;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;

public interface ILuceneSearcher {

    Document doc(int id) throws IOException;

    TopDocs search(Query query, int start, int length) throws Exception;

    Map<String, Fieldable[]> getDetails(int docId, String[] fields) throws Exception;

    void close() throws IOException;
}
