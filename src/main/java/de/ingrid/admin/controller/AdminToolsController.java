/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
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
package de.ingrid.admin.controller;

import de.ingrid.admin.Config;
import de.ingrid.admin.IKeys;
import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;
import de.ingrid.admin.service.CacheService;
import de.ingrid.admin.service.CommunicationService;
import de.ingrid.admin.service.PlugDescriptionService;
import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.utils.IRecordLoader;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.dsc.Column;
import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.idf.IdfTool;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.*;

@Controller
public class AdminToolsController extends AbstractController {

    protected static final Logger LOG = Logger.getLogger( AdminToolsController.class );

    private final CommunicationService _communication;

    private final HeartBeatPlug _plug;

    private final CacheService _cacheService;

    @Autowired
    private Config config;

    @Autowired
    private PlugDescriptionService plugDescriptionService;

    // private final PlugDescriptionService _plugDescriptionService;

    @Autowired
    public AdminToolsController(final CommunicationService communication, final HeartBeatPlug plug, final CacheService cacheService) {
        _communication = communication;
        _plug = plug;
        _cacheService = cacheService;
    }

    @RequestMapping(value = IUris.COMM_SETUP, method = RequestMethod.GET)
    public String getCommSetup(final ModelMap modelMap) {
        modelMap.addAttribute( "connected", _communication.isConnected() );
        return IViews.COMM_SETUP;
    }

    @RequestMapping(value = IUris.COMM_SETUP, method = RequestMethod.POST)
    public String postCommSetup(@RequestParam("action") final String action) {
        if ("shutdown".equals( action )) {
            _communication.shutdown();
        } else if ("restart".equals( action )) {
            _communication.restart();
        } else if ("start".equals( action )) {
            _communication.start();
        }
        return redirect( IUris.COMM_SETUP );
    }

    @RequestMapping(value = IUris.HEARTBEAT_SETUP, method = RequestMethod.GET)
    public String getHeartbeat(final ModelMap modelMap) {
        modelMap.addAttribute( "enabled", _plug.sendingHeartBeats() );
        modelMap.addAttribute( "accurate", _plug.sendingAccurate() );
        return IViews.HEARTBEAT_SETUP;
    }

    @RequestMapping(value = IUris.HEARTBEAT_SETUP, method = RequestMethod.POST)
    public String setHeartBeat(@RequestParam("action") final String action) throws IOException {
        if ("start".equals( action )) {
            _plug.startHeartBeats();
        } else if ("stop".equals( action )) {
            _plug.stopHeartBeats();
        } else if ("restart".equals( action )) {
            _plug.stopHeartBeats();
            _plug.startHeartBeats();
        }
        return redirect( IUris.HEARTBEAT_SETUP );
    }

    @RequestMapping(value = IUris.SEARCH, method = RequestMethod.GET)
    public String showView(final ModelMap modelMap, @RequestParam(value = "query", required = false) final String queryString) throws Exception {
        if (queryString != null) {
            modelMap.addAttribute( "query", queryString );
            final IngridQuery query = QueryStringParser.parse( queryString );

            final IngridHits results = _plug.search( query, 0, 20 );
            modelMap.addAttribute( "totalHitCount", results.length() );

            final IngridHit[] hits = results.getHits();
            // create requested fields list for search/detail
            final List<String> requestedFields = new ArrayList<String>(Arrays.asList(config.indexFieldTitle, config.indexFieldSummary));
            // add additional requested fields from configuration to be able to
            // add fields iplug specific. For example the iPlug-SE needs an field "url" to be added.
            requestedFields.addAll(Arrays.asList(config.searchRequestedFieldsAdditional.split("\\s*,\\s*")));
            final IngridHitDetail[] details = _plug.getDetails( hits, query, requestedFields.stream().toArray(String[]::new) );

            // convert details to map
            // this is necessary because it's not possible to access the
            // document-id by ${hit.documentId}
            final Map<String, IngridHitDetail> detailsMap = new HashMap<>();
            if (details != null) {
                for (final IngridHitDetail detail : details) {
                    if(detail != null){
                        try {
                            // if no title is given, then assume it might be an address
                            if (detail.getString("title").isEmpty()) {
                                detail.put( "title", detail.getArray( "t02_address.lastname")[0] + ", " + detail.getArray( "t02_address.firstname" )[0] );
                            }
                            // if url is an array convert it to an string
                            if (detail.containsKey("url") && detail.get("url") instanceof String[] && ((String[]) detail.get("url")).length > 0) {
                                detail.put("url", ((String[]) detail.get("url"))[0]);
                            }
                        } catch (Exception ignored) {}
                        detailsMap.put( detail.getDocumentId(), detail );
                    }
                }
            }

            modelMap.addAttribute( "hitCount", details != null ? details.length : 0);
            modelMap.addAttribute( "hits", detailsMap );
            modelMap.addAttribute( "details", _plug instanceof IRecordLoader );
        }

        return IViews.SEARCH;
    }

    @RequestMapping(value = IUris.SEARCH_DETAILS, method = RequestMethod.GET)
    public String showDetails(final ModelMap modelMap, @RequestParam(value = "id", required = false) final String id) throws Exception {
        if (!(_plug instanceof IRecordLoader) || id == null) {
            return IKeys.REDIRECT + IUris.SEARCH;
        }

        final IngridHit hit = new IngridHit();
        hit.setDocumentId( id );
        hit.setPlugId(plugDescriptionService.getPlugDescription().getPlugId());

        final IRecordLoader loader = (IRecordLoader) _plug;
        final Record record = loader.getRecord( hit );

        final Map<String, String> values = new HashMap<>();
        values.put( "title", "Kein Titel" );
        values.put( "summary", "Keine Beschreibung" );

        if (record != null) {
            values.put("data", StringEscapeUtils.escapeXml(IdfTool.getIdfDataFromRecord(record)));

            final Column[] columns = record.getColumns();
            if (columns != null) {
                for (final Column col : columns) {
                    values.put(col.getTargetName(), record.getValueAsString(col));
                }
            }
        } else {
            LOG.warn("No record found for ID: " + id);
        }

        modelMap.addAttribute( "values", values );

        return IViews.SEARCH_DETAILS;
    }

    @RequestMapping(value = IUris.CACHING, method = RequestMethod.GET)
    public String cachingGet(final ModelMap modelMap) {
        modelMap.addAttribute( "cache", _cacheService );
        return IViews.CACHING;
    }

    @RequestMapping(value = IUris.CACHING, method = RequestMethod.POST)
    public String cachingPost(final ModelMap modelMap, @ModelAttribute("cache") final CacheService cacheService) throws Exception {
        _cacheService.updateCache( cacheService );
        _cacheService.updateIngridCache();

        return cachingGet( modelMap );
    }
}
