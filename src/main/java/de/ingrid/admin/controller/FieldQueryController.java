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
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import net.weta.components.communication.configuration.XPathService;

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
import de.ingrid.admin.command.FieldQueryCommandObject;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.service.CommunicationService;
import de.ingrid.admin.validation.FieldQueryValidator;
import de.ingrid.utils.QueryExtension;
import de.ingrid.utils.query.FieldQuery;

@Controller
@SessionAttributes("plugDescription")
public class FieldQueryController extends AbstractController {
    
    private final CommunicationService _communicationInterface;

    private final FieldQueryValidator _validator;

    @Autowired
    public FieldQueryController(final CommunicationService communicationInterface, final FieldQueryValidator validator) {
        _communicationInterface = communicationInterface;
        _validator = validator;
    }

    @ModelAttribute("busUrls")
    public final String[] getBusUrls() throws Exception {
        boolean iBusDisabled = JettyStarter.getInstance().config.disableIBus;
        if (iBusDisabled) return new String[0];
        
        // open the communication file
        final XPathService pathService = new XPathService();
        pathService.registerDocument(_communicationInterface.getCommunicationFile());
        // count number of ibuses
        final int count = pathService.countNodes("/communication/client/connections/server");
        final String[] busUrls = new String[count];
        // add all ibus names
        for (int i = 0; i < count; i++) {
            busUrls[i] = pathService.parseAttribute("/communication/client/connections/server", "name", i);
        }

        return busUrls;
    }

    @ModelAttribute("fieldQuery")
    public final FieldQueryCommandObject getCommandObject() {
        return new FieldQueryCommandObject();
    }

    @RequestMapping(value = IUris.FIELD_QUERY, method = RequestMethod.GET)
    public String getFieldQuery(final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject) throws Exception {

        boolean iBusDisabled = JettyStarter.getInstance().config.disableIBus;
        if (iBusDisabled) return redirect(IUris.EXTRAS);
        
        // catching all field queries together in a list of maps
        final List<FieldQueryCommandObject> fields = getFields(commandObject.getQueryExtensions());
        modelMap.addAttribute("fields", fields);

        return IViews.FIELD_QUERY;
	}

    @RequestMapping(value = IUris.FIELD_QUERY, method = RequestMethod.POST)
    public String postFieldQuery(final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject,
            @ModelAttribute("fieldQuery") final FieldQueryCommandObject fieldQuery, final Errors errors,
            @RequestParam("action") final String action, @RequestParam(value = "id", required = false) final Integer id,
            @RequestParam("behaviour") final String behaviour)
            throws Exception {
        if ("add".equals(action)) {
            System.out.println("behaviour: " + behaviour);
            if (behaviour.equals(Config.QUERYTYPE_MODIFY)) {
                if (!_validator.validate(errors).hasErrors()) {
                    Config.addFieldQuery(commandObject, fieldQuery, behaviour);
                    // save changes in properties
                    JettyStarter.getInstance().config.addQueryExtensionsToProperties(fieldQuery);
                }
            } else {
                Config.addFieldQuery(commandObject, fieldQuery, behaviour);
                // save changes in properties
                JettyStarter.getInstance().config.addQueryExtensionsToProperties(fieldQuery);
            }
        } else if ("delete".equals(action)) {
            final FieldQueryCommandObject field = getFields(commandObject.getQueryExtensions()).get(id);
            deleteFieldQuery(commandObject, field);
            JettyStarter.getInstance().config.removeQueryExtensionsFromProperties(field);
        } else if ("submit".equals(action)) {
            // redirect to the first page of iPlug specific data
            return redirect(IUris.EXTRAS);
        }

        return getFieldQuery(modelMap, commandObject);
	}

    private List<FieldQueryCommandObject> getFields(final Map<String, QueryExtension> extensions) {
        final List<FieldQueryCommandObject> fields = new ArrayList<FieldQueryCommandObject>();
        if (extensions != null) {
            int pos = -1;
            for (final String key : extensions.keySet()) {
                final QueryExtension extension = extensions.get(key);
                final Set<Pattern> patterns = extension.getPatterns();
                if (patterns != null) {
                    for (final Pattern pattern : patterns) {
                        for (final FieldQuery fieldQuery : extension.getFieldQueries(pattern)) {
                            final FieldQueryCommandObject fq = new FieldQueryCommandObject();
                            fq.setBusUrl(key);
                            fq.setRegex(pattern.pattern());
                            fq.setKey(fieldQuery.getFieldName());
                            fq.setValue(fieldQuery.getFieldValue());
                            if (fieldQuery.isProhibited()) {
                                fq.setProhibited();
                            } else if (fieldQuery.isRequred()) {
                                fq.setRequired();
                            }
                            
                            // order patterns for better readability
                            if ("metainfo".equals(fieldQuery.getFieldName())) {
                                if ("query_allow".equals(fieldQuery.getFieldValue())) {
                                    fields.add(pos+1, fq);
                                } else if ("query_deny".equals(fieldQuery.getFieldValue())) {
                                    fields.add(++pos, fq);
                                } else {
                                    fields.add(fq);
                                }
                            } else {
                                fields.add(fq);
                            }
                        }
                    }
                }
            }
        }
        return fields;
    }

    
    private void deleteFieldQuery(final PlugdescriptionCommandObject commandObject,
            final FieldQueryCommandObject fieldQuery) {
        // get extension
        final QueryExtension extension = commandObject.getQueryExtensionContainer().getQueryExtension(fieldQuery.getBusUrl());
        // get patterns for field queries with equal pattern
        final Set<Pattern> patterns = extension.getPatterns();
        Set<FieldQuery> fieldQueries = null;
        for (final Pattern pattern : patterns) {
            if (pattern.pattern() == fieldQuery.getRegex()) {
                fieldQueries = extension.getFieldQueries(pattern);
                break;
            }
        }
        // find correct field query
        for (final FieldQuery fq : fieldQueries) {
            if (fieldQuery.getKey().equals(fq.getFieldName()) && fieldQuery.getValue().equals(fq.getFieldValue())
                    && fieldQuery.getProhibited() == fq.isProhibited() && fieldQuery.getRequired() == fq.isRequred()) {
                // delete it
                fieldQueries.remove(fq);
                break;
            }
        }
    }
}
