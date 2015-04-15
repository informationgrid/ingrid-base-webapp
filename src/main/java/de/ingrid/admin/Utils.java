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
package de.ingrid.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import de.ingrid.admin.object.Partner;
import de.ingrid.admin.object.Provider;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;

public class Utils {

    @SuppressWarnings("unchecked")
    public static List<Partner> getPartners(final IBus bus) throws Exception {
        final List<Partner> list = new ArrayList<Partner>();
        if (bus != null) {
            // get partners from bus
            final IngridQuery ingridQuery = new IngridQuery();
            ingridQuery.addField(new FieldQuery(false, false, "datatype", "management"));
            ingridQuery.addField(new FieldQuery(false, false, "management_request_type", "1"));
            ingridQuery.addField(new FieldQuery(false, false, "cache", "off")); 

            final IngridHits hits = bus.search(ingridQuery, 1000, 0, 0, 120000);
            if (hits.length() > 0) {
                final List<Object> partners = hits.getHits()[0].getArrayList("partner");
                for (final Object object : partners) {
                    final Map<String, Object> map = (Map<String, Object>) object;
                    final String partnerName = (String) map.get("name");
                    final String partnerId = (String) map.get("partnerid");
                    list.add(new Partner(partnerId, partnerName));
                }
            }
        } else {
            // get partners from file
            final Properties partners = new Properties();
            partners.load(Utils.class.getResourceAsStream("/partners.properties"));
            for (final Object key : partners.keySet()) {
                final String shortName = (String) key;
                final String displayName = partners.getProperty(shortName);
                list.add(new Partner(shortName, displayName));
            }
        }

        return list;
    }

    @SuppressWarnings("unchecked")
    public static List<Provider> getProviders(final IBus bus) throws Exception {
        final List<Provider> list = new ArrayList<Provider>();
        if (bus != null) {
            final IngridQuery ingridQuery = new IngridQuery();
            ingridQuery.addField(new FieldQuery(false, false, "datatype", "management"));
            ingridQuery.addField(new FieldQuery(false, false, "management_request_type", "2"));
            ingridQuery.addField(new FieldQuery(false, false, "cache", "off")); 

            final IngridHits hits = bus.search(ingridQuery, 1000, 0, 0, 120000);
            if (hits.length() > 0) {
                final List<Object> providers = hits.getHits()[0].getArrayList("provider");
                for (final Object object : providers) {
                    final Map<String, Object> map = (Map<String, Object>) object;
                    final String providerName = (String) map.get("name");
                    final String providerId = (String) map.get("providerid");
                    list.add(new Provider(providerId, providerName));
                }
            }
        }

        return list;
    }
    
    /**
     * Add a key with its value to the analyzed document, which will be used for indexing.
     * If the key is already present in the document, then the old value will be converted
     * to a list and the new value appended to it.
     * 
     * @param doc is the document for storing the key/value pairs
     * @param key is the name of the field to be stored
     * @param value is the value of the field
     */
    @SuppressWarnings("unchecked")
    public static void addToDoc(Map<String, Object> doc, String key, Object value) {
        Object obj = doc.get( key );
        // if key does not exist yet then just add key/value to document
        if (obj == null) {
            doc.put( key, value );
        } else {
            // if the object behind the key already is an array, we just add the value to this array
            if (obj instanceof List<?>) {
                ((List<Object>) obj).add( value );
            } else {
                // this is the second time where the same key is added, so we have to convert the object
                // into a list of objects
                ArrayList<Object> list = new ArrayList<Object>();
                list.add( obj );
                list.add( value );
                doc.put( key, list );
            }
        }
    }
}
