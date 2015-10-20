/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.Config;
import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;
import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.Utils;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.object.IDataType;
import de.ingrid.admin.object.Partner;
import de.ingrid.admin.object.Provider;
import de.ingrid.admin.service.CommunicationService;
import de.ingrid.admin.validation.AbstractValidator;
import de.ingrid.admin.validation.IErrorKeys;
import de.ingrid.admin.validation.PlugDescValidator;
import edu.emory.mathcs.backport.java.util.Arrays;

@Controller
@SessionAttributes("plugDescription")
public class GeneralController extends AbstractController {

    private final CommunicationService _communicationInterface;

    private final IDataType[] _dataTypes;

    private final PlugDescValidator _validator;

    private final CommunicationService _communicationService;
    
    private List<Partner> _partners = null;

    @Autowired
    public GeneralController(final CommunicationService communicationInterface,
            final CommunicationService communicationService, final PlugDescValidator validator,
            final IDataType... dataTypes) throws Exception {
        _communicationInterface = communicationInterface;
        _communicationService = communicationService;
        _validator = validator;
        _dataTypes = dataTypes;
    }

    @ModelAttribute("partners")
    public List<Partner> getPartners() throws Exception {
        if (_communicationInterface.isConnected(0)) {
            _partners = Utils.getPartners(_communicationInterface.getIBus());
            return _partners;
        }
        return new ArrayList<Partner>();
    }

    @ModelAttribute("dataTypes")
    public List<IDataType> injectDataTypes() {
        final List<IDataType> types = new ArrayList<IDataType>();
        for (final IDataType type : _dataTypes) {
            if (!type.isHidden()) {
                types.add(type);
            }
        }
        return types;
    }

    @RequestMapping(value = IUris.GENERAL, method = RequestMethod.GET)
    public String getGeneral(final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject, final Errors errors,
            @ModelAttribute("partners") final List<Partner> partners) throws Exception {

        // set up proxy service url
        commandObject.setProxyServiceURL(_communicationInterface.getPeerName());

        // test if plug is a igc plug
        boolean isIgc = false;
        if (_dataTypes != null) {
            for (final IDataType type : _dataTypes) {
                if (type.getName().equals(IDataType.DSC_ECS) || type.getName().equals(IDataType.DSC_ECS_ADDRESS)) {
                    isIgc = true;
                    break;
                }
            }
        }
        modelMap.addAttribute("isIgc", isIgc);

        // create map
        final SortedMap<String, List<Provider>> map = createPartnerProviderMap(partners, getProviders());
        modelMap.addAttribute("jsonMap", toJsonMap(map));

        // put original port in plugdescription if not already done
        if (!commandObject.containsKey("originalPort")) {
            commandObject.putInt("originalPort", commandObject.getIplugAdminGuiPort());
        }
        
        if (partners == null || partners.size() == 0) {
            modelMap.addAttribute("noManagement", true);
        }

        addForcedDatatypes(commandObject);
        
        return IViews.GENERAL;
    }

    @RequestMapping(value = IUris.GENERAL, method = RequestMethod.POST)
    public String postGeneral(final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject, final Errors errors,
            @ModelAttribute("partners") final List<Partner> partners) throws Exception {

        addForcedDatatypes(commandObject);
        
        String newPW = (String) errors.getFieldValue("newPassword");
        String currentPW = JettyStarter.getInstance().config.pdPassword;
        // only reject empty password if no password has been configured yet at all!
        if ((currentPW == null || currentPW.isEmpty()) && (newPW.isEmpty())) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "newPassword", AbstractValidator.getErrorKey(PlugdescriptionCommandObject.class, "newPassword", IErrorKeys.EMPTY));
        }
        
        // if no connection to iBus or no partners could be fetched then ignore the partner field!!! 
        if (_validator.validateGeneral(errors, !_communicationService.hasErrors() && !partners.isEmpty()).hasErrors()) {
            return getGeneral(modelMap, commandObject, errors, partners);
        }

        // add data type includes
        commandObject.addIncludedDataTypes(_dataTypes);
        
        setConfiguration( commandObject );
        
        return redirect(IUris.PARTNER);
    }

    @SuppressWarnings("unchecked")
    private void setConfiguration(PlugdescriptionCommandObject pd) {
        Config config = JettyStarter.getInstance().config;
        
        config.mainPartner = pd.getOrganisationPartnerAbbr();
        config.mainProvider = pd.getOrganisationAbbr();
        config.organisation = pd.getOrganisation();
        config.personTitle = pd.getPersonTitle();
        config.personName = pd.getPersonName();
        config.personSurname = pd.getPersonSureName();
        config.personPhone = pd.getPersonPhone();
        config.personEmail = pd.getPersonMail();
        config.datasourceName = pd.getDataSourceName();
        config.datasourceDescription = pd.getDataSourceDescription();
        config.datatypes = new ArrayList<String>(Arrays.asList( pd.getDataTypes() ) );
        config.guiUrl = pd.getIplugAdminGuiUrl();
        config.webappPort = pd.getIplugAdminGuiPort();
        String newPassword = pd.getNewPassword();
        if (newPassword != null && newPassword.trim().length() > 0) {
            String pw_hash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            config.pdPassword = pw_hash;
            pd.setIplugAdminPassword( pw_hash );
        }
        
    }

    private List<Provider> getProviders() throws Exception {
        if (_communicationInterface.isConnected(0)) {
            return Utils.getProviders(_communicationInterface.getIBus());
        }
        return new ArrayList<Provider>();
    }

    private final SortedMap<String, List<Provider>> createPartnerProviderMap(final List<Partner> partners,
            final List<Provider> providers) {
        final SortedMap<String, List<Provider>> map = new TreeMap<String, List<Provider>>();
        for (final Partner pa : partners) {
            final List<Provider> list = new ArrayList<Provider>();
            final Iterator<Provider> it = providers.iterator();
            final String shortName = pa.getShortName().substring(0, 2);
            while (it.hasNext()) {
                final Provider pr = it.next();
                if (pr.getShortName().startsWith(shortName)) {
                    list.add(pr);
                    it.remove();
                }
            }
            map.put(pa.getShortName(), list);
        }
        return map;
    }

    private String toJsonMap(final SortedMap<String, List<Provider>> map) {
        final StringBuilder result = new StringBuilder("{");
        for (final String key : map.keySet()) {
            result.append("'" + key + "':[");
            final Iterator<Provider> it = map.get(key).iterator();
            while (it.hasNext()) {
                final Provider p = it.next();
                result.append("{'shortName':'" + p.getShortName() + "','displayName':'" + p.getDisplayName() + "'}");
                if (it.hasNext()) {
                    result.append(",");
                }
            }
            result.append("]");
            if (key != map.lastKey()) {
                result.append(",");
            }
        }
        result.append("}");
        return result.toString();
    }

    private void addForcedDatatypes(PlugdescriptionCommandObject commandObject) {
        if (_dataTypes != null) {
            for (final IDataType type : _dataTypes) {
                if (type.getIsForced()) {
                    commandObject.addDataType(type.getName());
                }
            }
        }
    }
}
