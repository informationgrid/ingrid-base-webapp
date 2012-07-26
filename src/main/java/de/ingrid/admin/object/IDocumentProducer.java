package de.ingrid.admin.object;

import org.apache.lucene.document.Document;

import de.ingrid.utils.IConfigurable;

public interface IDocumentProducer extends IConfigurable {

	boolean hasNext();

	/** NOTICE: Can return null, e.g. when problems fetching data from "record" (or service) !
	 * @return Document for index OR NULL if nothing should be added to index ! 
	 */
	Document next();

}
