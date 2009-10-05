package de.ingrid.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class SchedulingController {
	
	@RequestMapping(value = "/base/scheduling.html", method = RequestMethod.GET)
	public String showView() {
		// this is still a dummy controller
		return "/base/scheduling";
	}
	
	@RequestMapping(value = "/base/scheduling.html", method = RequestMethod.POST)
	public String submit() {
		// this is still a dummy controller
		return "redirect:/base/indexing.html";
	}

}
