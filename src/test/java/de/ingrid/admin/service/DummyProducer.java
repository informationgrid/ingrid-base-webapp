/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.ingrid.admin.object.IDocumentProducer;
import de.ingrid.utils.PlugDescription;

public class DummyProducer implements IDocumentProducer {

    private List<Map<String, Object>> _dummys;

    private Iterator<Map<String, Object>> _iterator;

    @Override
    public boolean hasNext() {
        return _iterator.hasNext();
    }


    @Override
    public Map<String, Object> next() {
        return _iterator.next();
    }

    private Map<String, Object> createDocument(final String first, final String last, final float boost, final String url) {
        final Map<String, Object> doc = new HashMap<String, Object>();
        doc.put("title", first);
        doc.put("content", last);
        doc.put("boost", boost);
        doc.put("url", url);
        return doc;
    }

	@Override
	public void configure(PlugDescription arg0) {
		_dummys = new ArrayList<Map<String, Object>>();
		_dummys.add(createDocument("Max", "Ender", 0.1f, "http://aaa.de"));
		_dummys.add(createDocument("Marko", "Bauhardt", 0.2f, "http://bbb.de"));
		_dummys.add(createDocument("Andreas", "Kuester", 0.3f, "http://ccc.de"));
		_dummys.add(createDocument("Frank", "Henze", 0.4f, "http://ddd.de"));
		_dummys.add(createDocument("öStemmerTestÖ", "äStemmerTestÄ", 0.5f, "http://eee.de"));

		_iterator = _dummys.iterator();

	}
}
