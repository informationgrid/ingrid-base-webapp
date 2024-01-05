/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
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
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import de.ingrid.admin.object.IDocumentProducer;
import de.ingrid.admin.object.Partner;
import de.ingrid.admin.object.Provider;
import de.ingrid.elasticsearch.IndexInfo;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Utils {

    private static Log log = LogFactory.getLog(Utils.class);

    @SuppressWarnings("unchecked")
    public static List<Partner> getPartners(final IBus bus) throws Exception {
        final List<Partner> list = new ArrayList<>();
        if (bus != null) {
            // get partners from bus
            final IngridQuery ingridQuery = new IngridQuery();
            ingridQuery.addField(new FieldQuery(false, false, "datatype", "management"));
            ingridQuery.addField(new FieldQuery(false, false, "management_request_type", "1"));
            ingridQuery.addField(new FieldQuery(false, false, "cache", "off")); 

            final IngridHits hits = bus.search(ingridQuery, 1000, 0, 0, 120000);
            if (hits.length() > 0) {
                final List<Object> partners = hits.getHits()[0].getArrayList("partner");
                if (partners != null) {
                    for (final Object object : partners) {
                        final Map<String, Object> map = (Map<String, Object>) object;
                        final String partnerName = (String) map.get("name");
                        final String partnerId = (String) map.get("partnerid");
                        list.add(new Partner(partnerId, partnerName));
                    }
                } else {
                    log.warn("No partners found");
                }
            } else {
                log.warn("No iBus result when requesting for partners");
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
        final List<Provider> list = new ArrayList<>();
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
    
    public static IndexInfo getIndexInfo(IDocumentProducer producer, Config config) {
        IndexInfo indexInfo = producer.getIndexInfo();
        if (indexInfo == null) {
            indexInfo = new IndexInfo();
            indexInfo.setToIndex( config.index );
            if (config.indexAlias != null && config.indexAlias.length() > 0) {
                indexInfo.setToAlias( config.indexAlias );
            } else {
                indexInfo.setToAlias( config.index );
            }
            indexInfo.setToType( config.indexType );
            indexInfo.setDocIdField( config.indexIdFromDoc );
        }

        return indexInfo;
    }
    
    public static void addDatatypeToIndex(String index, String type, Config config) {
        if (config.datatypesOfIndex != null) {
            String[] types = config.datatypesOfIndex.get( index );
            if (types != null) {
                if (!ArrayUtils.contains( types, type )) {
                    config.datatypesOfIndex.put( index, (String[]) ArrayUtils.add( types, type ) );
                    if (!config.datatypes.contains( type )) {
                        config.datatypes.add( type );
                    }
                }
            }
        }
    }

    public static Set<String> getUnionOfDatatypes(Map<String, String[]> datatypesOfIndex) {
        Set<String> allUniqueTypes = new LinkedHashSet<>();
        if (datatypesOfIndex != null) {
            Set<String> indices = datatypesOfIndex.keySet();
            for (String index : indices) {
                allUniqueTypes.addAll( Arrays.asList( datatypesOfIndex.get( index )) );
            }
        }
        return allUniqueTypes;
    }
    
}
