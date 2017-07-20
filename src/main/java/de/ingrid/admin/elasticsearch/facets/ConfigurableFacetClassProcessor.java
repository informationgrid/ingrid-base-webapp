/*
 * **************************************************-
 * ingrid-search-utils
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
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
/**
 * 
 */
package de.ingrid.admin.elasticsearch.facets;

import java.util.List;
import java.util.Map;

/**
 * @author joachim
 *
 */
public class ConfigurableFacetClassProcessor implements IFacetDefinitionProcessor {

    /**
     * Holds the filter definitions: {<class_id>, {<query_fragment>, <query_fragment_substitution}}
     * 
     */
    private Map<String, Map<String, String>> facetFilterDefinitions = null;
    
    /* (non-Javadoc)
     * @see de.ingrid.search.utils.IFacetDefinitionProcessor#process(java.util.List)
     */
    @Override
    public void process(List<FacetDefinition> facetDefinitions) {
        for (FacetDefinition facetDef : facetDefinitions) {
            if (facetDef.getClasses() != null) { 
                for (FacetClassDefinition facetClass : facetDef.getClasses()) {
                    for (String filterFacetClass : facetFilterDefinitions.keySet()) {
                        if (facetClass.getName().equals(filterFacetClass)) {
                            for (String facetClassQueryFragment : facetFilterDefinitions.get(filterFacetClass).keySet()) {
                                if (facetClass.getFragment() != null && facetClass.getFragment().equals(facetClassQueryFragment)) {
                                    facetClass.setQueryFragment(facetFilterDefinitions.get(filterFacetClass).get(facetClassQueryFragment));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void setFacetFilterDefinitions(Map<String, Map<String, String>> facetFilterDefinitions) {
        this.facetFilterDefinitions = facetFilterDefinitions;
    }

}
