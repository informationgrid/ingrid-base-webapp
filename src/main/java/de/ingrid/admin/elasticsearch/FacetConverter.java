/*
 * **************************************************-
 * ingrid-iplug-se-iplug
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
package de.ingrid.admin.elasticsearch;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.InternalFilter;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.UnmappedTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.admin.elasticsearch.converter.QueryConverter;
import de.ingrid.admin.elasticsearch.facets.FacetClassDefinition;
import de.ingrid.admin.elasticsearch.facets.FacetDefinition;
import de.ingrid.admin.elasticsearch.facets.FacetUtils;
import de.ingrid.admin.elasticsearch.facets.IFacetDefinitionProcessor;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.ParseException;
import de.ingrid.utils.queryparser.QueryStringParser;

@Service
public class FacetConverter {
    
    private static Log log = LogFactory.getLog( FacetConverter.class );
    
    @Autowired(required=false)
    private List<IFacetDefinitionProcessor> facetDefinitionProcessors = new ArrayList<IFacetDefinitionProcessor>();

    private QueryConverter queryConverter;
    
    @Autowired
    public FacetConverter(QueryConverter qc) {
        this.queryConverter = qc;
    }

    public List<AbstractAggregationBuilder> getAggregations(IngridQuery ingridQuery) {
        // get all FacetDefinitions from the Query
        List<FacetDefinition> defs = FacetUtils.getFacetDefinitions(ingridQuery);
        
        // filter those facets in case some have to be mapped to another value (see IGCTopicsSearchPreProcessor)
        for (IFacetDefinitionProcessor facetdefProcessor : facetDefinitionProcessors) {
            facetdefProcessor.process(defs);
        }
        
        List<AbstractAggregationBuilder> aggregations = new ArrayList<AbstractAggregationBuilder>();
        
        for (FacetDefinition facetDefinition : defs) {
            String name = facetDefinition.getName();
            String field = facetDefinition.getField();
            List<FacetClassDefinition> classes = facetDefinition.getClasses();
            AbstractAggregationBuilder aggr = null;
            if (classes != null) {
                for (FacetClassDefinition fClass : classes) {
                    IngridQuery facetQuery;
                    try {
                        facetQuery = QueryStringParser.parse( fClass.getFragment() );
                        aggr = AggregationBuilders.filter( fClass.getName() ).filter( queryConverter.convert( facetQuery ) );
                        aggregations.add( aggr );
                    } catch (ParseException e) {
                        log.error( "Error during parsing facets." );
                        e.printStackTrace();
                    }
                }
                
            } else {
                aggr = AggregationBuilders.terms( name ).field( field );
                aggregations.add( aggr );
            }
        }
        
        return aggregations;
    }

    public IngridDocument convertFacetResultsToDoc(SearchResponse response) {
        IngridDocument facets = new IngridDocument();
        
        List<Aggregation> aggregations = response.getAggregations().asList();
        
        // aggregations seem to become null if no index was created yet
        if (aggregations != null) {
            for (Aggregation aggregation : aggregations) {
                if ( aggregation.getClass() == UnmappedTerms.class ) {
                    // nothing to do here!?
                } else if ( aggregation.getClass() == StringTerms.class ) {
                    StringTerms partnerAgg = (StringTerms) aggregation;
                    for (Bucket bucket : partnerAgg.getBuckets()) {
                        facets.put(aggregation.getName() + ":" + bucket.getKey(), bucket.getDocCount() );
                    }
                } else if ( aggregation.getClass() == InternalFilter.class ) {
                    InternalFilter agg = (InternalFilter) aggregation;
                    facets.put(aggregation.getName(), agg.getDocCount() );
                    
                } else {
                    throw new RuntimeException( "Aggregation Class not supported: " + aggregation.getClass() );
                }
            }
        }
        
        return facets; 
    }

}
