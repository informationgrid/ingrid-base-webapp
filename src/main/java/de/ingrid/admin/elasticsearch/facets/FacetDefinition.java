/*
 * **************************************************-
 * ingrid-search-utils
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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

public class FacetDefinition {

    /**
     * Should contain the index field where this facet belongs to.
     */
    private String field;

    /**
     * id = FacetName:FacetValue
     * 
     * e.g.: datatype:iso, metaclass:0, partner:bw
     */
    private String name;
    
    /**
     * Optional, a query queryFragment that narrows the facet classes in field definition.
     * 
     */
    private String queryFragment;
    

    /**
     * 
     * @element-type FacetClassDefinition
     */
    private List<FacetClassDefinition> classes;
    

    public FacetDefinition(String facetName, String field) {
        this.name = facetName;
        this.field = field;
        this.classes = new ArrayList<FacetClassDefinition>();
    }

    public Boolean hasFacetClass(String clazz) {
        return null;
    }

    public void addFacetClass(FacetClassDefinition facetClass) {
        this.classes.add(facetClass);        
    }

    public void setClasses(List<FacetClassDefinition> classes) {
        this.classes = classes;
    }

    /**
     * Return the FacetClasses if any, otherwise return null.
     * @return
     */
    public List<FacetClassDefinition> getClasses() {
        if (classes.size() == 0)
            return null;
        return classes;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public String getQueryFragment() {
        return queryFragment;
    }

    public void setQueryFragment(String queryFragment) {
        this.queryFragment = queryFragment;
    }


}
