package de.ingrid.admin.object;

import org.apache.lucene.document.Document;

public interface IDocumentProducer {

    boolean hasNext();
    
    Document next();

    void initialize() throws Exception;

}
