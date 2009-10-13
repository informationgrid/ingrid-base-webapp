package de.ingrid.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.ingrid.admin.service.IndexScheduler;

@Controller
public class SchedulingController {

    public static final String SCHEDULING_URI = "/base/scheduling.html";

    public static final String SCHEDULING_VIEW = "/base/scheduling";

    public static final String DELETE_PATTERN_URI = "/base/deletePattern.html";

    final IndexScheduler _scheduler;

    @Autowired
    public SchedulingController(final IndexScheduler scheduler) {
        _scheduler = scheduler;
    }

    @RequestMapping(value = SCHEDULING_URI, method = RequestMethod.GET)
    public String getScheduling(final ModelMap modelMap) {
        modelMap.addAttribute("pattern", _scheduler.getPattern());
        return SCHEDULING_VIEW;
	}

    @RequestMapping(value = SCHEDULING_URI, method = RequestMethod.POST)
    public String postScheduling(final ModelMap modelMap,
            @RequestParam(value = "hour", required = false) final String hour,
            @RequestParam(value = "minute", required = false) final String minute,
            @RequestParam(value = "dayOfWeek", required = false) final String dayOfWeek,
            @RequestParam(value = "dayOfMonth", required = false) final String dayOfMonth,
            @RequestParam(value = "pattern", required = false) String pattern) {
        // daily: m h * * *
        // weekly: m h * * w
        // monthly: m h d * *
        // pattern: m h d M w
        if (pattern == null) {
            pattern = (minute != null ? minute : "*") + " " + (hour != null ? hour : "*") + " "
                    + (dayOfMonth != null ? dayOfMonth : "*") + " * "
                    + (dayOfWeek != null ? dayOfWeek : "*");
        }
        _scheduler.setPattern(pattern);

        return "redirect:" + IndexController.INDEXING_URI;
	}

    @RequestMapping(value = DELETE_PATTERN_URI, method = RequestMethod.POST)
    public String delete(final ModelMap modelMap) {
        _scheduler.deletePattern();
        return getScheduling(modelMap);
    }

}
