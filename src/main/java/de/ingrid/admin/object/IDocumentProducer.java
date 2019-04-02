/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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
package de.ingrid.admin.object;

import de.ingrid.admin.elasticsearch.IndexInfo;
import de.ingrid.utils.ElasticDocument;
import de.ingrid.utils.IConfigurable;

public interface IDocumentProducer extends IConfigurable {

	boolean hasNext();

	/** NOTICE: Can return null, e.g. when problems fetching data from "record" (or service) !
	 * @return Document for index OR NULL if nothing should be added to index ! 
	 */
	ElasticDocument next();

	IndexInfo getIndexInfo();
	
	/**
	 * Get the number of documents to be indexed. If it cannot be determined
	 * then null is returned.
	 * @return the number of docs, otherwise null
	 */
	Integer getDocumentCount();
}
