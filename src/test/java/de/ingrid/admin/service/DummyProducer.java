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
package de.ingrid.admin.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;

import de.ingrid.admin.object.IDocumentProducer;
import de.ingrid.utils.PlugDescription;

public class DummyProducer implements IDocumentProducer {

    private List<Document> _dummys;

    private Iterator<Document> _iterator;

    @Override
    public boolean hasNext() {
        return _iterator.hasNext();
    }


    @Override
    public Document next() {
        return _iterator.next();
    }

    private Document createDocument(final String first, final String last, final String gender, final String birthday) {
        final Document doc = new Document();
        doc.add(createField("first", first));
        doc.add(createField("last", last));
        doc.add(createField("gender", gender));
        doc.add(createField("birthdate", birthday));
        return doc;
    }

    private Field createField(final String key, final String value) {
        return new Field(key, value, Store.YES, Index.ANALYZED);
    }

	@Override
	public void configure(PlugDescription arg0) {
		_dummys = new ArrayList<Document>();
		_dummys.add(createDocument("Max", "Ender", "male", "08.12.1988"));
		_dummys.add(createDocument("Marko", "Bauhardt", "male", "30.07.1978"));
		_dummys.add(createDocument("Andreas", "Kuester", "male", "01.01.1970"));
		_dummys.add(createDocument("Frank", "Henze", "male", "01.01.1970"));
		_dummys.add(createDocument("öStemmerTestÖ", "äStemmerTestÄ", "üStemmerTestÜ", "ßStemmerTestß"));

		_iterator = _dummys.iterator();

	}
}
