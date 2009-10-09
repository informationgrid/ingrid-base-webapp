package de.ingrid.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

@Controller
public class SearchController {

    private final HeartBeatPlug _plug;

    @Autowired
    public SearchController(final HeartBeatPlug plug) {
        _plug = plug;
    }

	@RequestMapping(value = "/base/search.html", method = RequestMethod.GET)
    public String showView(final ModelMap modelMap,
            @RequestParam(value = "query", required = false) final String queryString) throws Exception {
	    if (queryString != null) {
            modelMap.addAttribute("query", queryString);
	        final IngridQuery query = QueryStringParser.parse(queryString);

            final IngridHits results = _plug.search(query, 0, 20);
            modelMap.addAttribute("totalHitCount", results.length());

            final IngridHit[] hits = results.getHits();
            final IngridHitDetail[] details = _plug.getDetails(hits, query, null);

            modelMap.addAttribute("hitCount", details.length);
            modelMap.addAttribute("hits", details);
	    }

		return "/base/search";
	}

	@RequestMapping(value = "/base/searchDetails.html", method = RequestMethod.GET)
	public String showDetails() {
		// this is still a dummy controller
		return "/base/searchDetails";
	}

}
