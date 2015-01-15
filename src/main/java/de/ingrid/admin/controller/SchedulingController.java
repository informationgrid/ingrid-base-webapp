/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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
package de.ingrid.admin.controller;

import it.sauronsoftware.cron4j.InvalidPatternException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;
import de.ingrid.admin.search.IndexScheduler;

@Controller
public class SchedulingController extends AbstractController {

    final IndexScheduler _scheduler;

    @Autowired
    public SchedulingController(final IndexScheduler scheduler) {
        _scheduler = scheduler;
    }

    @RequestMapping(value = IUris.SCHEDULING, method = RequestMethod.GET)
    public String getScheduling(final ModelMap modelMap) {
        modelMap.addAttribute("pattern", _scheduler.getPattern());
        return IViews.SCHEDULING;
	}

    @RequestMapping(value = IUris.SCHEDULING, method = RequestMethod.POST)
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
        
        try {
            _scheduler.setPattern(pattern);
        } catch (InvalidPatternException e) {
            modelMap.addAttribute("error", "Invalid pattern!");
            _scheduler.deletePattern();
            return IViews.SCHEDULING;
        } catch (Exception e) {
            modelMap.addAttribute("error", "An error occured!");
            _scheduler.deletePattern();
            return IViews.SCHEDULING;
        }

        return redirect(IUris.INDEXING);
	}

    @RequestMapping(value = IUris.DELETE_PATTERN, method = RequestMethod.POST)
    public String delete(final ModelMap modelMap) {
        _scheduler.deletePattern();
        return getScheduling(modelMap);
    }

}
