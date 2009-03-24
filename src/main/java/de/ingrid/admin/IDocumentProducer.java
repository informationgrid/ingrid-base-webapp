package de.ingrid.admin;

import org.apache.lucene.document.Document;

public interface IDocumentProducer {

    boolean hasNext();
    
    Document next();

    void initialize() throws Exception;

}
