package de.ingrid.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class SearchController {
	
	@RequestMapping(value = "/base/search.html", method = RequestMethod.GET)
	public String showView() {
		// this is still a dummy controller
		return "/base/search";
	}
	
	@RequestMapping(value = "/base/searchDetails.html", method = RequestMethod.GET)
	public String showDetails() {
		// this is still a dummy controller
		return "/base/searchDetails";
	}

}
