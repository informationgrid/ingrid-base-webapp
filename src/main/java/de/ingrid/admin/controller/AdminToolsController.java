package de.ingrid.admin.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

@Controller
public class AdminToolsController extends AbstractController {

    protected static final Logger LOG = Logger.getLogger(AdminToolsController.class);

    private final CommunicationService _communication;

    private final HeartBeatPlug _plug;

    private final CacheService _cacheService;

    private final PlugDescriptionService _plugDescriptionService;

    @Autowired
    public AdminToolsController(final CommunicationService communication, final HeartBeatPlug plug,
            final CacheService cacheService, final PlugDescriptionService plugDescriptionService) throws Exception {
        _communication = communication;
        _plug = plug;
        _cacheService = cacheService;
        _plugDescriptionService = plugDescriptionService;
    }

    @RequestMapping(value = IUris.COMM_SETUP, method = RequestMethod.GET)
    public String getCommSetup(final ModelMap modelMap) {
        modelMap.addAttribute("connected", _communication.isConnected());
        return IViews.COMM_SETUP;
    }

    @RequestMapping(value = IUris.COMM_SETUP, method = RequestMethod.POST)
    public String postCommSetup(@RequestParam("action") final String action) throws Exception {
        if ("shutdown".equals(action)) {
            _communication.shutdown();
        } else if ("restart".equals(action)) {
            _communication.restart();
        } else if ("start".equals(action)) {
            _communication.start();
        }
        return redirect(IUris.COMM_SETUP);
    }

    @RequestMapping(value = IUris.HEARTBEAT_SETUP, method = RequestMethod.GET)
    public String getHeartbeat(final ModelMap modelMap) {
        modelMap.addAttribute("enabled", _plug.sendingHeartBeats());
        modelMap.addAttribute("accurate", _plug.sendingAccurate());
        return IViews.HEARTBEAT_SETUP;
    }

    @RequestMapping(value = IUris.HEARTBEAT_SETUP, method = RequestMethod.POST)
    public String setHeartBeat(@RequestParam("action") final String action) throws IOException {
        if ("start".equals(action)) {
            _plug.startHeartBeats();
        } else if ("stop".equals(action)) {
            _plug.stopHeartBeats();
        } else if ("restart".equals(action)) {
            _plug.stopHeartBeats();
            _plug.startHeartBeats();
        }
        return redirect(IUris.HEARTBEAT_SETUP);
    }

    @RequestMapping(value = IUris.SEARCH, method = RequestMethod.GET)
    public String showView(final ModelMap modelMap, @RequestParam(value = "query", required = false) final String queryString) throws Exception {
        if (queryString != null) {
            modelMap.addAttribute("query", queryString);
            final IngridQuery query = QueryStringParser.parse(queryString);

            final IngridHits results = _plug.search(query, 0, 20);
            modelMap.addAttribute("totalHitCount", results.length());

            final IngridHit[] hits = results.getHits();
            final IngridHitDetail[] details = _plug.getDetails(hits, query, new String[] {});

            // convert details to map
            // this is necessary because it's not possible to access the
            // document-id by ${hit.documentId}
            final Map<Integer, IngridHitDetail> detailsMap = new HashMap<Integer, IngridHitDetail>();
            if (details != null) {
                for (final IngridHitDetail detail : details) {
                    detailsMap.put(detail.getDocumentId(), detail);
                }
            }

            modelMap.addAttribute("hitCount", details.length);
            modelMap.addAttribute("hits", detailsMap);
            modelMap.addAttribute("details", _plug instanceof IRecordLoader);
        }

        return IViews.SEARCH;
    }

    @RequestMapping(value = IUris.SEARCH_DETAILS, method = RequestMethod.GET)
    public String showDetails(final ModelMap modelMap, @RequestParam(value = "id", required = false) final Integer id)
            throws Exception {
        if (!(_plug instanceof IRecordLoader) || id == null) {
            return IKeys.REDIRECT + IUris.SEARCH;
        }

        final IngridHit hit = new IngridHit();
        hit.setDocumentId(id);

        final IRecordLoader loader = (IRecordLoader) _plug;
        final Record record = loader.getRecord(hit);

        final Map<String, String> values = new HashMap<String, String>();
        values.put("title", "Kein Titel");
        values.put("summary", "Keine Beschreibung");
        final Column[] columns = record.getColumns();
        if (columns != null) {
            for (final Column col : columns) {
                values.put(col.getTargetName(), record.getValueAsString(col));
            }
        }
        modelMap.addAttribute("values", values);

        return IViews.SEARCH_DETAILS;
    }

    @RequestMapping(value = IUris.CACHING, method = RequestMethod.GET)
    public String cachingGet(final ModelMap modelMap) {
        modelMap.addAttribute("cache", _cacheService);
        return IViews.CACHING;
    }

    @RequestMapping(value = IUris.CACHING, method = RequestMethod.POST)
    public String cachingPost(final ModelMap modelMap, @ModelAttribute("cache") final CacheService cacheService)
            throws Exception {
        _cacheService.updateCache(cacheService);
        _cacheService.updateIngridCache();

        return cachingGet(modelMap);
    }
}
