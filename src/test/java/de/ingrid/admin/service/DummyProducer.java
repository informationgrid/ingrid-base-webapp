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
import java.util.Iterator;
import java.util.List;

import de.ingrid.admin.object.IDocumentProducer;
import de.ingrid.utils.ElasticDocument;
import de.ingrid.utils.PlugDescription;

public class DummyProducer implements IDocumentProducer {

    private List<ElasticDocument> _dummys;

    private Iterator<ElasticDocument> _iterator;

    private int model;
    
    public DummyProducer() {}
    
    public DummyProducer(int config) {
        this.model = config;
    }

    @Override
    public boolean hasNext() {
        return _iterator.hasNext();
    }


    @Override
    public ElasticDocument next() {
        return _iterator.next();
    }

    private ElasticDocument createDocument(final String id, final String first, final String last, final float boost, final String url) {
        final ElasticDocument doc = new ElasticDocument();
        doc.put( "id", id);
        doc.put( "title", first);
        doc.put( "content", last);
        doc.put( "boost", boost);
        doc.put( "url", url);
        doc.put( "mylist", "first entry" );
        
        if ("id#2".equals( id )) {
            doc.put( "mylist", "second entry" );
        }
        
        return doc;
    }

	@Override
	public void configure(PlugDescription arg0) {
		_dummys = new ArrayList<ElasticDocument>();
		ElasticDocument specialDoc = createDocument("id#1", "Max", "Ender", 0.1f, "http://aaa.de");
		specialDoc.put( "specialField", "secret" );
		
        _dummys.add(specialDoc );
		_dummys.add(createDocument("id#2", "Marko", "Bauhardt", 0.2f, "http://bbb.de"));
		if (model == 1) {
		    _dummys.add(createDocument("id#3", "Marko", "Kuester", 0.3f, "http://ccc.de"));
		} else {
		    _dummys.add(createDocument("id#3", "Andreas", "Kuester", 0.3f, "http://ccc.de"));
		}
		_dummys.add(createDocument("id#4", "Frank", "Henze", 0.4f, "http://ddd.de"));
		_dummys.add(createDocument("id#4", "FrankDuplicate", "HenzeDuplicate", 0.4f, "http://dddDuplicate.de"));
		_dummys.add(createDocument("id#5", "öStemmerTestÖ", "äStemmerTestÄ", 0.5f, "http://eee.de"));

		_iterator = _dummys.iterator();

	}
}
