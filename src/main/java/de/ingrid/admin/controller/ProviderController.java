/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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

import de.ingrid.admin.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.object.Provider;
import de.ingrid.admin.service.CommunicationService;
import de.ingrid.admin.validation.IErrorKeys;
import de.ingrid.admin.validation.PlugDescValidator;

@Controller
@SessionAttributes("plugDescription")
public class ProviderController extends AbstractController {

    private final CommunicationService _communicationInterface;

    private final PlugDescValidator _validator;

    private final CommunicationService _communicationService;

    private boolean noProviderAvailable;

    private List<Provider> providers;

    private final Config config;

    @Autowired
    public ProviderController(final CommunicationService communicationInterface,
                              final CommunicationService communicationService, final PlugDescValidator validator, Config config)
            throws Exception {
        _communicationInterface = communicationInterface;
        _communicationService = communicationService;
        _validator = validator;
        this.config = config;
    }

    @RequestMapping(value = IUris.PROVIDER, method = RequestMethod.GET)
    public String getProvider(final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject) throws Exception {

        final List<Provider> providerList = getProviders(commandObject.getPartners());
        modelMap.addAttribute("providerList", providerList);

        final List<Provider> addedProviders = new ArrayList<Provider>();
        for (final String shortName : commandObject.getProviders()) {
        	Provider p = getByShortName(providerList, shortName);
            if(p == null){
        		commandObject.removeProvider(shortName);
        	}else{
        	    addedProviders.add(p);	
        	}
        }
        modelMap.addAttribute("providers", addedProviders);
        
        return IViews.PROVIDER;
    }

    @RequestMapping(value = IUris.PROVIDER, method = RequestMethod.POST)
    public String postProvider(final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject, final Errors errors,
            @RequestParam("action") final String action,
            @RequestParam(value = "provider", required = false) final String provider,
            @RequestParam(value = "id", required = false) final String id) throws Exception {

        if ("add".equals(action)) {
            if (StringUtils.isEmptyOrWhiteSpace(provider)) {
                _validator.rejectError(errors, "providers", IErrorKeys.EMPTY);
            } else {
                commandObject.addProvider(provider);
                config.provider = commandObject.getProviders();
            }
        } else if ("delete".equals(action)) {
            if (!id.equals(commandObject.getOrganisationAbbr())) {
                commandObject.removeProvider(id);
                config.provider = commandObject.getProviders();
            }
        } else if ("submit".equals(action)) {
            if (noProviderAvailable || !_validator.validateProviders(errors, !_communicationService.hasErrors()).hasErrors()) {
                return redirect(IUris.EXTRAS);
            }
        }

        return getProvider(modelMap, commandObject);
    }

    List<Provider> getProviders(final String... partners) throws Exception {
        final List<Provider> providerList = new ArrayList<Provider>();
        if (_communicationInterface.isConnected(0)) {
            for (final Provider provider : Utils.getProviders(_communicationInterface.getIBus())) {
                if (hasPartner(partners, provider)) {
                    providerList.add(provider);
                }
            }
        }
        
        if (providerList.isEmpty()) {
            this.noProviderAvailable = true;
        } else {
            this.noProviderAvailable = false;
        }
        providers = providerList;
        return providers;
    }

    private boolean hasPartner(final String[] partners, final Provider provider) {
        for (final String partner : partners) {
            if (provider.getShortName().startsWith(partner.equals("bund") ? "bu" : partner)) {
                return true;
            }
        }
        return false;
    }

    private final Provider getByShortName(final List<Provider> providers, final String shortName) {
        for (final Provider provider : providers) {
            if (provider.getShortName().equals(shortName)) {
                return provider;
            }
        }
        return null;
    }
}
