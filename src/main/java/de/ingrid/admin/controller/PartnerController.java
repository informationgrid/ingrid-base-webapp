/*
 * **************************************************-
 * ingrid-base-webapp
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
package de.ingrid.admin.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.Config;
import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;
import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.StringUtils;
import de.ingrid.admin.Utils;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.object.Partner;
import de.ingrid.admin.service.CommunicationService;
import de.ingrid.admin.validation.IErrorKeys;
import de.ingrid.admin.validation.PlugDescValidator;

@Controller
@SessionAttributes("plugDescription")
public class PartnerController extends AbstractController {

    private final CommunicationService _communicationInterface;

    private final PlugDescValidator _validator;

    private List<Partner> partners;

	@Autowired
    public PartnerController(final CommunicationService communicationInterface, final PlugDescValidator validator)
			throws Exception {
		_communicationInterface = communicationInterface;
        _validator = validator;
	}

	@ModelAttribute("partnerList")
	public List<Partner> getPartners() throws Exception {
	    if (_communicationInterface.isConnected(0)) {
	        partners = Utils.getPartners(_communicationInterface.getIBus());
	        return partners;
	    }
	    return new ArrayList<Partner>();
	}

    @RequestMapping(value = IUris.PARTNER, method = RequestMethod.GET)
    public String getPartner(final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject,
            @ModelAttribute("partnerList") final List<Partner> partnerList) {

        final List<Partner> addedPartners = new ArrayList<Partner>();
        for (final String shortName : commandObject.getPartners()) {
            addedPartners.add(getByShortName(partnerList, shortName));
        }
        modelMap.addAttribute("partners", addedPartners);
        
        if (partners == null || partners.size() == 0) {
            modelMap.addAttribute("noManagement", true);
        }
        
        return IViews.PARTNER;
	}

    @RequestMapping(value = IUris.PARTNER, method = RequestMethod.POST)
    public String postPartner(final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject, final Errors errors,
            @ModelAttribute("partnerList") final List<Partner> partnerList,
            @RequestParam("action") final String action,
            @RequestParam(value = "partner", required = false) final String partner,
            @RequestParam(value = "id", required = false) final String id) {

        Config config = JettyStarter.getInstance().config;
        if ("add".equals(action)) {
            if (StringUtils.isEmptyOrWhiteSpace(partner)) {
                _validator.rejectError(errors, "partners", IErrorKeys.EMPTY);
            } else {
                commandObject.addPartner(partner);
                config.partner = commandObject.getPartners();
            }
        } else if ("delete".equals(action)) {
            if (!id.equals(commandObject.getOrganisationPartnerAbbr())) {
                commandObject.removePartner(id);
                config.partner = commandObject.getPartners();
                // remove all provider that belong to the partner
                for (String provider : commandObject.getProviders()) {
                    if (provider.startsWith( id + "_" ) || ("bund".equals( id ) && provider.startsWith( "bu_" ))) {
                        commandObject.removeProvider( provider );
                    };
                }
                config.provider = commandObject.getProviders();
            }
        } else if ("submit".equals(action)) {
            if (partnerList.isEmpty() || !_validator.validatePartners(errors).hasErrors()) {
                return redirect(IUris.PROVIDER);
            }
        }

        return getPartner(modelMap, commandObject, partnerList);
	}

    private final Partner getByShortName(final List<Partner> partners, final String shortName) {
        for (final Partner partner : partners) {
            if (partner.getShortName().equals(shortName)) {
                return partner;
            }
        }
        return null;
    }
}
