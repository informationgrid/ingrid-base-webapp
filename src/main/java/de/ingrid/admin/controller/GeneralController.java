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

import de.ingrid.admin.Config;
import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;
import de.ingrid.admin.Utils;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.object.IDataType;
import de.ingrid.admin.object.IDocumentProducer;
import de.ingrid.admin.object.Partner;
import de.ingrid.admin.object.Provider;
import de.ingrid.admin.service.CommunicationService;
import de.ingrid.admin.validation.AbstractValidator;
import de.ingrid.admin.validation.IErrorKeys;
import de.ingrid.admin.validation.PlugDescValidator;
import de.ingrid.elasticsearch.IndexInfo;
import org.apache.commons.lang.ArrayUtils;
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

import java.util.*;

@Controller
@SessionAttributes("plugDescription")
public class GeneralController extends AbstractController {

    private final CommunicationService _communicationInterface;

    private final IDataType[] _dataTypes;

    private final PlugDescValidator _validator;

    private final CommunicationService _communicationService;
    
    private List<Partner> _partners = null;

    private final Config config;

    @Autowired(required=false)
    private List<IDocumentProducer> docProducer = new ArrayList<IDocumentProducer>();

    @Autowired
    public GeneralController(final CommunicationService communicationInterface,
                             final CommunicationService communicationService, final PlugDescValidator validator,
                             Config config, final IDataType... dataTypes) throws Exception {
        _communicationInterface = communicationInterface;
        _communicationService = communicationService;
        _validator = validator;
        _dataTypes = dataTypes;
        this.config = config;
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

        addForcedDatatypes(commandObject);
        
        List<IndexInfo> indices = new ArrayList<IndexInfo>();
        if (config.datatypesOfIndex == null) {
            for (IDocumentProducer producer : docProducer) {
                IndexInfo indexInfo = Utils.getIndexInfo( producer, config );
                String datatypes = (String) config.getOverrideProperties().get( "plugdescription.dataType." + indexInfo.getIdentifier()  );
                if (datatypes == null) {
                    if (config.datatypes != null) {
                        commandObject.setDatatypesOfIndex( indexInfo.getIdentifier(), config.datatypes.toArray( new String[0] ) );
                    } else {
                        // only add forced datatypes on very first configuration
                        commandObject.setDatatypesOfIndex( indexInfo.getIdentifier(), commandObject.getDataTypes() );
                    }
                } else {
                    commandObject.setDatatypesOfIndex( indexInfo.getIdentifier(), datatypes.split( "," ) );
                }
                indices.add( indexInfo );
            }
            config.datatypesOfIndex = commandObject.getDatatypesOfIndex();
        } else {
            for (IDocumentProducer producer : docProducer) {
                indices.add( Utils.getIndexInfo( producer, config ) );
            }
            commandObject.setDatatypesOfIndex(config.datatypesOfIndex);
        }
        
        modelMap.addAttribute( "indices", indices );
        return IViews.GENERAL;
    }

    @RequestMapping(value = IUris.GENERAL, method = RequestMethod.POST)
    public String postGeneral(final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject, final Errors errors,
            @ModelAttribute("partners") final List<Partner> partners) throws Exception {

        String newPW = (String) errors.getFieldValue("newPassword");
        String currentPW = config.pdPassword;
        // only reject empty password if no password has been configured yet at all!
        if ((currentPW == null || currentPW.isEmpty()) && (newPW.isEmpty())) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "newPassword", AbstractValidator.getErrorKey(PlugdescriptionCommandObject.class, "newPassword", IErrorKeys.EMPTY));
        }
        
        // if no connection to iBus or no partners could be fetched then ignore the partner field!!! 
        if (_validator.validateGeneral(errors, !_communicationService.hasErrors() && !partners.isEmpty()).hasErrors()) {
            return getGeneral(modelMap, commandObject, errors, partners);
        }

        setConfiguration( commandObject );
        
        // add forced datatypes
        addForcedDatatypesToConfig();
        
        // add data type includes
        addIncludedDataTypes( _dataTypes );
        
        return redirect(IUris.PARTNER);
    }
    
    private void addIncludedDataTypes(final IDataType... types) {
        // for all indices
        // check if index has the parent field and add then the included ones
        for (IDocumentProducer producer : docProducer) {
            IndexInfo indexInfo = Utils.getIndexInfo( producer, config );
            List<String> included = new ArrayList<String>();
            String[] datatypesOfIndex = config.datatypesOfIndex.get( indexInfo.getIdentifier() );
            
            // check all data types
            for (final String dataType : datatypesOfIndex) {
                // find correct idatatype
                for (final IDataType type : types) {
                    if (type.getName().equals(dataType)) {
                        // if found add all included data types
                        if (type.getIncludedDataTypes() != null) {
                            for (final IDataType include : type.getIncludedDataTypes()) {
                                // add to all datatypes field
                                if (!config.datatypes.contains( include.getName() )) {
                                    config.datatypes.add( include.getName() );
                                }
                                if (ArrayUtils.contains( datatypesOfIndex, dataType )) {
                                    included.add( include.getName() );
                                }
                            }
                        }
                    }
                }
            }
            config.datatypesOfIndex.put( indexInfo.getIdentifier(), (String[]) ArrayUtils.addAll( datatypesOfIndex, included.toArray() ) );
        }
    }
  
    private void setConfiguration(PlugdescriptionCommandObject pd) {

        config.mainPartner = pd.getOrganisationPartnerAbbr();
        config.partner = pd.getPartners();
        config.mainProvider = pd.getOrganisationAbbr();
        config.provider = pd.getProviders();
        config.organisation = pd.getOrganisation();
        config.personTitle = pd.getPersonTitle();
        config.personName = pd.getPersonName();
        config.personSurname = pd.getPersonSureName();
        config.personPhone = pd.getPersonPhone();
        config.personEmail = pd.getPersonMail();
        config.datasourceName = pd.getDataSourceName();
        config.datasourceDescription = pd.getDataSourceDescription();
        config.datatypesOfIndex = pd.getDatatypesOfIndex();
        config.datatypes.clear();
        if (pd.getDatatypesOfIndex().isEmpty()) {
            config.datatypes.addAll( Arrays.asList(pd.getDataTypes()) );
        } else {
            config.datatypes.addAll( Utils.getUnionOfDatatypes(config.datatypesOfIndex) );
        }
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

    private void addForcedDatatypesToConfig() {
        if (_dataTypes != null) {
            for (final IDataType type : _dataTypes) {
                if (type.getIsForced()) {
                    for (IDocumentProducer producer : docProducer) {
                        IndexInfo indexInfo = Utils.getIndexInfo( producer, config );
                        Utils.addDatatypeToIndex( indexInfo.getIdentifier(), type.getName(), config);
                    }
                    if (docProducer.size() == 0) {
                        config.datatypes.add( type.getName() );
                    }
                }
            }
        }
    }
    
    private void addForcedDatatypes(PlugdescriptionCommandObject commandObject) {
        if (_dataTypes != null) {
            for (final IDataType type : _dataTypes) {
                if (type.getIsForced()) {
                    commandObject.addDataType(type.getName());
                    if (!config.datatypes.contains( type.getName() )) config.datatypes.add( type.getName() ); 
                    for (IDocumentProducer producer : docProducer) {
                        IndexInfo indexInfo = Utils.getIndexInfo( producer, config );
                        commandObject.addDatatypesOfIndex( indexInfo.getIdentifier(), type.getName() );
                    }
                }
            }
        }
    }
}
