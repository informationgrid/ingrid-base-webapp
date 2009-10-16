package de.ingrid.admin.object;

import org.apache.lucene.document.Document;

import de.ingrid.utils.IConfigurable;

public interface IDocumentProducer extends IConfigurable {

	boolean hasNext();

	Document next();

}
