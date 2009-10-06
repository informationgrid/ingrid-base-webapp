package de.ingrid.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class FinishController {

	@RequestMapping(value = "/base/finish.html", method = RequestMethod.GET)
	public String showView() {
		// this is still a dummy controller
		return "/base/finish";
	}
}
