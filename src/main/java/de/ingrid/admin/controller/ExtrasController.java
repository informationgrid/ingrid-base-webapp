/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.object.Extras;
import de.ingrid.admin.validation.ExtrasValidator;
import de.ingrid.utils.query.IngridQuery;

/**
 * This controller is used to allow configuration of not so common parameters.
 * Without configuration the controller skips to the next page. If you want to
 * configure this controller, just write another controller, which autowires
 * this controller and call the "show_***"-functions of the elements that you
 * want to have visible.
 * 
 * @author Andre Wallat
 *
 */

@Controller
@SessionAttributes("plugDescription")
public class ExtrasController extends AbstractController {

    private List<String> _visibleEntries = new ArrayList<String>();
    
    private final ExtrasValidator _validator;

    @Autowired
    public ExtrasController(final ExtrasValidator validator) {
        _validator = validator;
    }

    @RequestMapping(value = {IUris.EXTRAS}, method = RequestMethod.GET)
    public String getExtras(final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject plugDescription) {
        if (_visibleEntries.isEmpty()) {
            return redirect(IUris.IPLUG_WELCOME);
        }
        
        Extras e = new Extras();
        // check if the forced parameter (for ranking) was set before
        if (plugDescription.containsKey("forceAddRankingOff")) {
            e.setShowInUnranked(plugDescription.getBoolean("forceAddRankingOff"));
        } else {
            e.setShowInUnranked(false);
        }
        
        // write object into model
        modelMap.addAttribute("extrasConfig", e);
        
        for (String entry : _visibleEntries) {
            modelMap.addAttribute(entry, "true");
        }
        return IViews.EXTRAS;
    }

    @RequestMapping(value = IUris.EXTRAS, method = RequestMethod.POST)
    public String postExtras(@ModelAttribute("plugDescription") final PlugdescriptionCommandObject plugDescription, 
            final BindingResult errors,
            @ModelAttribute("extrasConfig") final Extras commandObject) {
        if (_validator.validate(errors).hasErrors()) {
            return IViews.EXTRAS;
        }
        
        // remember previous ranking values
        boolean isScore = plugDescription.containsRankingType("score");
        boolean isDate  = plugDescription.containsRankingType("date");
        
        // write Ranking-information after list got emptied
        if (plugDescription.getArrayList(IngridQuery.RANKED) != null )
            plugDescription.getArrayList(IngridQuery.RANKED).clear();
        
        // parameter "off" decides about visibility in unranked results
        plugDescription.setRankinTypes(isScore, isDate, commandObject.getShowInUnranked());
        
        // set to remember next time starting Administration-page 
        plugDescription.putBoolean("forceAddRankingOff", commandObject.getShowInUnranked());
        
        return redirect(IUris.IPLUG_WELCOME);
    }
    
    public void show_ShowInUnranked() {
        _visibleEntries.add("showShowInUnranked");
    }
    
    public boolean anyVisibleEntries() {
        return !_visibleEntries.isEmpty();
    }
}
