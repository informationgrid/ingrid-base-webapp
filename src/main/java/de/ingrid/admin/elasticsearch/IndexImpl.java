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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.ingrid.admin.Config;
import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.elasticsearch.converter.QueryConverter;
import de.ingrid.utils.ElasticDocument;
import de.ingrid.utils.IDetailer;
import de.ingrid.utils.IRecordLoader;
import de.ingrid.utils.ISearcher;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.dsc.Column;
import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.query.IngridQuery;

@Component
public class IndexImpl implements ISearcher, IDetailer, IRecordLoader {

    public static final String DETAIL_URL = "url";

    private static Logger log = Logger.getLogger( IndexImpl.class );
    
    @Autowired
    private QueryBuilderService queryBuilderService;

    private QueryConverter queryConverter;

    private FacetConverter facetConverter;

    private static final String ELASTIC_SEARCH_INDEX = "es_index";

    private static final String ELASTIC_SEARCH_INDEX_TYPE = "es_type";

    private Config config;

    private String[] detailFields;

    private IndexManager indexManager;

    @Autowired
    public IndexImpl(IndexManager indexManager, QueryConverter qc, FacetConverter fc) {
        this.config = JettyStarter.getInstance().config;
        this.indexManager = indexManager;
        this.detailFields = (String[]) ArrayUtils.addAll( new String[] { config.indexFieldTitle, config.indexFieldSummary }, config.additionalSearchDetailFields );

        try {
            this.queryConverter = qc;
            this.facetConverter = fc;

            log.info( "Elastic Search Settings: " + indexManager.printSettings() );

        } catch (Exception e) {
            log.error( "Error during initialization of ElasticSearch-Client!" );
            e.printStackTrace();
        }

    }

    @SuppressWarnings("rawtypes")
    @Override
    public IngridHits search(IngridQuery ingridQuery, int startHit, int num) {

        // convert InGrid-query to QueryBuilder
        QueryBuilder query = queryConverter.convert( ingridQuery );

        QueryBuilder funcScoreQuery = null;
        if (config.indexEnableBoost) {
            funcScoreQuery = queryConverter.addScoreModifier( query );
        }

        boolean isLocationSearch = containsBoundingBox(ingridQuery);
        boolean hasFacets = ingridQuery.containsKey( "FACETS" );
        String[] instances = getSearchInstances( ingridQuery );

        // request grouping information from index if necessary
        // see IndexImpl.getHitsFromResponse for usage
        String groupedBy = ingridQuery.getGrouped();
        String[] fields = null;
        if (IngridQuery.GROUPED_BY_PARTNER.equalsIgnoreCase( groupedBy )) {
            fields = new String[] { IngridQuery.PARTNER };
        } else if (IngridQuery.GROUPED_BY_ORGANISATION.equalsIgnoreCase( groupedBy )) {
            fields = new String[] { IngridQuery.PROVIDER };
        } else if (IngridQuery.GROUPED_BY_DATASOURCE.equalsIgnoreCase( groupedBy )) {
            // the necessary value id the results ID
        }

        String[] indexNames = JettyStarter.getInstance().config.docProducerIndices;
        
        if (indexNames.length == 0) {
            log.warn( "No configured index to search on!" );
            return new IngridHits( 0, new IngridHit[0] );
        }
        
        BoolQueryBuilder indexTypeFilter = queryBuilderService.createIndexTypeFilter( indexNames );
        
        // if we are remotely connected to an elasticsearch node then get the real indices of the aliases
        // otherwise we also get the results from other indices, since an alias can contain several indices!
        List<String> realIndices = new ArrayList<String>();
        for (int i=0; i < indexNames.length; i++) {
            String[] indexNameAlias = indexNames[i].split( ":" );
            String realIndex = indexManager.getIndexNameFromAliasName( indexNameAlias[0], indexNameAlias[1] );
            if (realIndex != null) {
                realIndices.add( realIndex );
            }
        }
        indexNames = realIndices.toArray( new String[0] );
        
        
        // search prepare
        SearchRequestBuilder srb = indexManager.getClient().prepareSearch( indexNames  )
                .setQuery( config.indexEnableBoost ? funcScoreQuery : query ) // Query
                .setQuery( config.indexEnableBoost 
                        ? QueryBuilders.boolQuery().must( funcScoreQuery ).must( indexTypeFilter )
                        : QueryBuilders.boolQuery().must( query ).must( indexTypeFilter ) ) // Query
                .setFrom( startHit ).setSize( num ).setExplain( false );

        // search only in defined types within the index, if defined
        if (instances.length > 0) {
            srb.setTypes( instances );
        }

        if (fields == null) {
            srb = srb.setFetchSource( false );
        } else {
            srb = srb.storedFields( fields );
        }

        // Filter for results only with location information
        if (isLocationSearch) {
            srb.setPostFilter( QueryBuilders.existsQuery( "x1" ) );
        }

        // pre-processing: add facets/aggregations to the query
        if (hasFacets) {
            List<AbstractAggregationBuilder> aggregations = facetConverter.getAggregations( ingridQuery );
            for (AbstractAggregationBuilder aggregation : aggregations) {
                srb.addAggregation( aggregation );
            }
        }

        if (log.isDebugEnabled()) {
            log.debug( "Final Elastic Search Query: \n" + srb );
        }

        // search!
        try {
            SearchResponse searchResponse = srb.execute().actionGet();

            // convert to IngridHits
            IngridHits hits = getHitsFromResponse( searchResponse, ingridQuery );

            // post-processing: extract and convert facets to InGrid-Document
            if (hasFacets) {
                // add facets from response
                IngridDocument facets = facetConverter.convertFacetResultsToDoc( searchResponse );
                hits.put( "FACETS", facets );
            }

            return hits;
        } catch (SearchPhaseExecutionException ex) {
            log.error( "Search failed on index: " + indexNames, ex );
            return new IngridHits( 0, new IngridHit[0] );
        }
    }

    private boolean containsBoundingBox(IngridQuery ingridQuery) {
        boolean found = ingridQuery.containsField( "x1" );
        
        // also try to look in clauses 
        if (!found) {
            for (IngridQuery clause : ingridQuery.getAllClauses()) {
                if (clause.containsField( "x1" )) {
                    return true;
                }
            }
        }
        return found;
    }

    /**
     * Check first the query for a hidden field which contains the information of the instances to search in for. If there's none, then use
     * the defined one in the configuration. The parameter in the query should be only used for an internal search within the iPlug.
     * 
     * @param ingridQuery
     * @return
     */
    private String[] getSearchInstances(IngridQuery ingridQuery) {
        String[] instances = (String[]) ingridQuery.getArray( "searchInInstances" );
        if (instances == null || instances.length == 0) {
            instances = JettyStarter.getInstance().config.indexSearchInTypes.toArray( new String[0] );
        }
        return instances;
    }

    /**
     * Create InGrid hits from ES hits. Add grouping information.
     * 
     * @param searchResponse
     * @param ingridQuery
     * @return
     */
    private IngridHits getHitsFromResponse(SearchResponse searchResponse, IngridQuery ingridQuery) {
        for (ShardSearchFailure failure : searchResponse.getShardFailures()) {
            log.error( "Error searching in index: " + failure.reason() );
        }

        SearchHits hits = searchResponse.getHits();

        // the size will not be bigger than it was requested in the query with
        // 'num'
        // so we can convert from long to int here!
        int length = (int) hits.getHits().length;
        int totalHits = (int) hits.getTotalHits();
        IngridHit[] hitArray = new IngridHit[length];
        int pos = 0;
        
        if (log.isDebugEnabled()) {
            log.debug( "Received " + length + " from " + totalHits + " hits." );
        }

        String groupBy = ingridQuery.getGrouped();
        for (SearchHit hit : hits.getHits()) {
            IngridHit ingridHit = new IngridHit( config.communicationProxyUrl, hit.getId(), -1, hit.getScore() );
            ingridHit.put( ELASTIC_SEARCH_INDEX, hit.getIndex() );
            ingridHit.put( ELASTIC_SEARCH_INDEX_TYPE, hit.getType() );

            // get grouing information, add if exist
            String groupValue = null;
            if (IngridQuery.GROUPED_BY_PARTNER.equalsIgnoreCase( groupBy )) {
                SearchHitField field = hit.getField( IngridQuery.PARTNER );
                if (field != null) {
                    groupValue = field.getValue().toString();
                }
            } else if (IngridQuery.GROUPED_BY_ORGANISATION.equalsIgnoreCase( groupBy )) {
                SearchHitField field = hit.getField( IngridQuery.PROVIDER );
                if (field != null) {
                    groupValue = field.getValue().toString();
                }
            } else if (IngridQuery.GROUPED_BY_DATASOURCE.equalsIgnoreCase( groupBy )) {
                groupValue = config.communicationProxyUrl;
                if (config.groupByUrl) {
                    try {
                        groupValue = new URL( hit.getId() ).getHost();
                    } catch (MalformedURLException e) {
                        log.warn( "can not group url: " + groupValue, e );
                    }
                }
            }
            if (groupValue != null) {
                ingridHit.addGroupedField( groupValue );
            }

            hitArray[pos] = ingridHit;
            pos++;
        }

        IngridHits ingridHits = new IngridHits( totalHits, hitArray );

        return ingridHits;
    }

    @Override
    public IngridHitDetail getDetail(IngridHit hit, IngridQuery ingridQuery, String[] requestedFields) {
        for (int i = 0; i < requestedFields.length; i++) {
            requestedFields[i] = requestedFields[i].toLowerCase();
        }
        String documentId = hit.getDocumentId();
        String fromIndex = hit.getString( ELASTIC_SEARCH_INDEX );
        String fromType = hit.getString( ELASTIC_SEARCH_INDEX_TYPE );
        String[] allFields = (String[]) ArrayUtils.addAll( detailFields, requestedFields );

        // We have to search here again, to get a highlighted summary of the result!
        QueryBuilder query = QueryBuilders.boolQuery().must( QueryBuilders.matchQuery( IngridDocument.DOCUMENT_UID, documentId ) ).must( queryConverter.convert( ingridQuery ) );
        
        HighlightBuilder hb = new HighlightBuilder();
        hb.field("config.indexFieldSummary");

        // search prepare
        SearchRequestBuilder srb = indexManager.getClient().prepareSearch( fromIndex ).setTypes( fromType )
                .setQuery( query ) // Query
                .setFrom( 0 )
                .setSize( 1 )
                .highlighter( hb )
                .storedFields( allFields )
                .setExplain( false );

        SearchResponse searchResponse = srb.execute().actionGet();

        SearchHits dHits = searchResponse.getHits();
        SearchHit dHit = dHits.getAt( 0 );

        String title = "untitled";
        if (dHit.getField( config.indexFieldTitle ) != null) {
            title = (String) dHit.getField( config.indexFieldTitle ).getValue();
        }
        String summary = "";
        // try to get the summary first from the highlighted fields
        if (dHit.getHighlightFields().containsKey( config.indexFieldSummary )) {
            summary = StringUtils.join( dHit.getHighlightFields().get( config.indexFieldSummary ).fragments(), " ... " );
            // otherwise get it from the original field
        } else if (dHit.getField( config.indexFieldSummary ) != null) {
            summary = (String) dHit.getField( config.indexFieldSummary ).getValue();
        }

        IngridHitDetail detail = new IngridHitDetail( hit, title, summary );

        addPlugDescriptionInformations( detail, requestedFields );

        detail.setDocumentId( documentId );
        if (requestedFields != null) {
            for (String field : requestedFields) {
                if (dHit.getField( field ) != null) {
                    if (dHit.getField( field ).getValues() instanceof List){
                        if(dHit.getField( field ).getValues().size() > 1){
                            detail.put( field, dHit.getField( field ).getValues());
                        }else{
                            if (dHit.getField( field ).getValue() instanceof String) {
                                detail.put( field, new String[] { dHit.getField( field ).getValue() } );
                            } else {
                                detail.put( field, dHit.getField( field ).getValue() );
                            }
                        }
                    } else if (dHit.getField( field ).getValue() instanceof String) {
                        detail.put( field, new String[] { dHit.getField( field ).getValue() } );
                    } else {
                        detail.put( field, dHit.getField( field ).getValue() );
                    }
                }
            }
        }

        // add additional fields to detail object (such as url for iPlugSE)
        for (String extraDetail : config.additionalSearchDetailFields) {
            SearchHitField field = dHit.getFields().get( extraDetail );
            if (field != null) {
                detail.put( extraDetail, field.getValue() );
            }
        }

        return detail;
    }

    private void addPlugDescriptionInformations(IngridHitDetail detail, String[] fields) {
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].equals( PlugDescription.PARTNER )) {
                detail.setArray( PlugDescription.PARTNER, config.partner );
            } else if (fields[i].equals( PlugDescription.PROVIDER )) {
                detail.setArray( PlugDescription.PROVIDER, config.provider );
            }
        }
    }

    @Override
    public IngridHitDetail[] getDetails(IngridHit[] hits, IngridQuery ingridQuery, String[] requestedFields) {
        for (int i = 0; i < requestedFields.length; i++) {
            requestedFields[i] = requestedFields[i].toLowerCase();
        }
        List<IngridHitDetail> details = new ArrayList<IngridHitDetail>();
        for (IngridHit hit : hits) {
            details.add( getDetail( hit, ingridQuery, requestedFields ) );
        }
        return details.toArray( new IngridHitDetail[0] );
    }

    @Override
    // FIXME: is destroyed automatically via the BEAN!!!
    public void close() {
        try {
            indexManager.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ElasticDocument getDocById(Object id) {
        String idAsString = String.valueOf( id );
        String[] indexNames = JettyStarter.getInstance().config.docProducerIndices;
        // itereate over all indices until document was found
        for (String indexName : indexNames) {
            String[] aliasInfo = indexName.split( ":" );
            Map<String, Object> source = indexManager.getClient().prepareGet( aliasInfo[0], null, idAsString )
                    .setFetchSource( config.indexFieldsIncluded, config.indexFieldsExcluded )
                    .execute().actionGet().getSource();
            
            if (source != null) {
                return new ElasticDocument( source );
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Record getRecord(IngridHit hit) throws Exception {
        String documentId = hit.getDocumentId();
        ElasticDocument document = getDocById( documentId );
        String[] fields = document.keySet().toArray( new String[0] );
        Record record = new Record();
        for (String name : fields) {
            Object stringValue = document.get( name );
            if (stringValue instanceof List) {
                for (String item : (List<String>) stringValue) {
                    Column column = new Column( null, name, null, true );
                    column.setTargetName( name );
                    record.addColumn( column, item );
                }
            } else {
                Column column = new Column( null, name, null, true );
                column.setTargetName( name );
                record.addColumn( column, stringValue );
            }
        }
        return record;
    }

}
