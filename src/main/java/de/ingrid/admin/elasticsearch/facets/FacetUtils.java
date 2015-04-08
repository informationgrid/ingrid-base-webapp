/*
 * **************************************************-
 * ingrid-search-utils
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
package de.ingrid.admin.elasticsearch.facets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.query.IngridQuery;

public class FacetUtils {

    @SuppressWarnings("unused")
    private static Logger LOG = Logger.getLogger(FacetUtils.class);

    public static String getCacheKeyName(String facetName, String facetClassName) {
        return facetName + ":" + facetClassName;
    }

    public static String getFacetNameFromFacetClass(String facetClassName) {
        int pos = facetClassName.indexOf(':');
        if (pos >= 0) {
            return facetClassName.substring(0, pos - 1);
        } else {
            return null;
        }
    }

    /**
     * Find all definitions of facets inside a query and check if we already do
     * have a FacetClassDefinition for it. If a FacetClassDefinition does not
     * exist then create it. If no FacetClassDefinitions were found for a Facet,
     * then we have to do a time consuming index search to find all possible
     * values for this FacetClass (but maximum of MAX_VALUES). This definition
     * will be checked in the FacetRegistry. If it's not there then a FacetClass
     * will be produced by the FacetClassProducer, which also will contain all
     * the docids in a BitSet, which is needed for a Facet-Search.
     */
    @SuppressWarnings( { "unchecked" })
    public static List<FacetDefinition> getFacetDefinitions(IngridQuery query) {
        List<FacetDefinition> facetDefs = new ArrayList<FacetDefinition>();

        List<IngridDocument> facets = (List<IngridDocument>) query.get("FACETS");
        if (facets == null)
            return null;

        // iterate through all facets
        for (IngridDocument facet : facets) {
            IngridDocument aFacet = facet;
            String facetName = (String) aFacet.get("id");
            String facetField = (String) aFacet.get("field");
            if (facetField == null) {
                facetField = facetName;
            }
            String facetFragment = (String) aFacet.get("query");
            FacetDefinition fd = new FacetDefinition(facetName, facetField);
            fd.setQueryFragment(facetFragment);

            List<Map<String, String>> facetClasses = (List<Map<String, String>>) aFacet.get("classes");
            // if facet classes were defined then look through those and create
            // their queries as String
            if (facetClasses != null) {
                String facetClassName;
                for (Map<String, String> facetClass : facetClasses) {
                    facetClassName = facetClass.get("id");
                    fd.addFacetClass(new FacetClassDefinition(FacetUtils.getCacheKeyName(facetName, facetClassName), facetClass.get("query")));
                }
            }
            facetDefs.add(fd);
        }
        return facetDefs;
    }

}
