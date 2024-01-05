/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
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
import de.ingrid.admin.command.CommunicationCommandObject;
import de.ingrid.admin.service.CommunicationService;
import de.ingrid.admin.service.PlugDescriptionService;
import de.ingrid.admin.validation.CommunicationValidator;
import de.ingrid.admin.validation.IErrorKeys;
import de.ingrid.elasticsearch.ElasticConfig;
import de.ingrid.utils.PlugDescription;
import net.weta.components.communication.configuration.XPathService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.InputStream;
import java.util.List;

@Controller
public class CommunicationConfigurationController extends AbstractController {

    public static final int DEFAULT_TIMEOUT = 10;

    public static final int DEFAULT_MAXIMUM_SIZE = 1048576;

    public static final int DEFAULT_THREAD_COUNT = 100;

    private String _defaultProxyServiceUrl = "";

    private final CommunicationService _communicationService;

    private final CommunicationValidator _validator;

    private PlugDescriptionService _plugDescriptionService;

    private final Config config;

    @Autowired(required = false)
    private ElasticConfig elasticConfig;

    @Autowired
    public CommunicationConfigurationController(final CommunicationService communicationService,
                                                final PlugDescriptionService pdService,
                                                final CommunicationValidator validator, Config config) {
        _communicationService = communicationService;
        _plugDescriptionService = pdService;
        _validator = validator;
        this.config = config;
    }

    @ModelAttribute("communication")
    public CommunicationCommandObject createCommandObject() throws Exception {
        // create command object
        final CommunicationCommandObject commandObject = new CommunicationCommandObject();

        // create the xpath
        final XPathService communication = new XPathService();

        // open communication file
        final File communicationFile = _communicationService.getCommunicationFile();
        
        if (communicationFile == null) return null;

        // bus count
        Integer count = 0;

        if (communicationFile.exists()) {
            // only save proxy service url
            communication.registerDocument(communicationFile);
            commandObject.setProxyServiceUrl(communication.parseAttribute("/communication/client", "name"));
            count = getBusCount(communication);
        } else {
            commandObject.setProxyServiceUrl( config.communicationProxyUrl );
        }

        if (count < 1) {
            // open template file
            final InputStream inputStream = CommunicationConfigurationController.class
                    .getResourceAsStream("/communication-template.xml");
            communication.registerDocument(inputStream);
            // save all default values
            if (null == commandObject.getProxyServiceUrl()) {
                _defaultProxyServiceUrl = communication.parseAttribute("/communication/client", "name");
                commandObject.setProxyServiceUrl(_defaultProxyServiceUrl);
            }
            commandObject.setBusProxyServiceUrl(communication.parseAttribute(
                    "/communication/client/connections/server", "name"));
            commandObject.setIp(communication.parseAttribute("/communication/client/connections/server/socket", "ip"));
            commandObject.setPort(Integer.parseInt(communication.parseAttribute(
                    "/communication/client/connections/server/socket", "port")));
        }

        return commandObject;
    }

    @ModelAttribute("busses")
    public List<CommunicationCommandObject> existingBusses() {
        List<CommunicationCommandObject> ibusses = config.ibusses;
        for (int i = 0; i < ibusses.size(); i++) {
            if (_communicationService.isConnected(i)) {
                ibusses.get( i ).setIsConnected( true );
            } else {
                ibusses.get( i ).setIsConnected( false );
            }
        }
        return ibusses;
    }

    @ModelAttribute("noBus")
    public Boolean noBus() {
        return _communicationService.hasErrors();
    }

    @RequestMapping(value = IUris.COMMUNICATION, method = RequestMethod.GET)
    public String getCommunication() {
        boolean iBusDisabled = config.disableIBus;
        
        if (iBusDisabled) {
            return redirect(IUris.WORKING_DIR);
        }
        return IViews.COMMUNICATION;
    }

    @RequestMapping(value = IUris.COMMUNICATION, method = RequestMethod.POST)
    public String postCommunication(final ModelMap modelMap,
            @ModelAttribute("communication") final CommunicationCommandObject commandObject, final Errors errors,
            @RequestParam(value = "action", required = false) final String action,
            @RequestParam(value = "id", required = false) final Integer id)
            throws Exception {
        if (null != action && !"".equals(action)) {
            final File communicationFile = _communicationService.getCommunicationFile();
            final XPathService communication = openCommunication(communicationFile);

            boolean tryToAdd = false;

            if ("submit".equals(action)) {
                // set proxy url
                if (_validator.validateProxyUrl(errors, _defaultProxyServiceUrl).hasErrors()) {
                    return IViews.COMMUNICATION;
                }
                setProxyUrl(commandObject.getProxyServiceUrl());

                if (!communicationFile.exists() || getBusCount(communication) == 0) {
                    tryToAdd = true;
                }
            }

            if ("add".equals(action) || tryToAdd) {
                // set proxy url
                if (!_validator.validateProxyUrl(errors, _defaultProxyServiceUrl).hasErrors()) {
                    setProxyUrl(commandObject.getProxyServiceUrl());
                }
                // add new bus
                if (_validator.validateBus(errors).hasErrors()) {
                    return IViews.COMMUNICATION;
                }
                
                config.ibusses.add( commandObject );

            } else if ("delete".equals(action)) {
                // delete bus
                config.ibusses.remove( id.intValue() );
            } else if ("set".equals(action)) {
                // set base bus
                CommunicationCommandObject newDefault = config.ibusses.remove( id.intValue() );
                config.ibusses.add( 0, newDefault );
            }

            // save the new data
            config.writeCommunication(config.communicationLocation, config.ibusses);
            
            PlugDescription plugDescription = _plugDescriptionService.getPlugDescription();
            if (plugDescription != null) {
                plugDescription.setProxyServiceURL( commandObject.getProxyServiceUrl() );
                _plugDescriptionService.savePlugDescription( plugDescription );
            }
            
            config.writeCommunicationToProperties();

            // when busses have been switched we need to restart communication with new config-file
            if ("set".equals(action) || "add".equals(action)) { // || "delete".equals(action)) {
                _communicationService.restart();
            }
            
            // update command object and existing busses
            //modelMap.addAttribute("communication", createCommandObject());
            //modelMap.addAttribute("busses", existingBusses());

            // submit complete?!
            if ("submit".equals(action)) {
                if (!communicationFile.exists() || getBusCount(communication) == 0) {
                    _validator.rejectError(errors, "busProxyServiceUrl", IErrorKeys.MISSING);
                    return IViews.COMMUNICATION;
                }
                // restart communication interface
                _communicationService.restart();
                if (_communicationService.isConnected(0)) {
                    // redirect to next step
                    return redirect(IUris.WORKING_DIR);
                }
                modelMap.addAttribute("noBus", _communicationService.hasErrors());
                return IViews.COMMUNICATION;
            }
        }

        return redirect( IViews.COMMUNICATION + ".html" );
    }

    private XPathService openCommunication(final File communicationFile) throws Exception {
        // first of all create directories if necessary
        if (communicationFile.getParentFile() != null) {
            if (!communicationFile.getParentFile().exists()) {
                communicationFile.getParentFile().mkdirs();
            }
        }

        // open template xml or communication file
        final XPathService communication = new XPathService();
        if (!communicationFile.exists()) {
            final InputStream inputStream = CommunicationConfigurationController.class
                    .getResourceAsStream("/communication-template.xml");
            communication.registerDocument(inputStream);
        } else {
            communication.registerDocument(communicationFile);
        }

        return communication;
    }

    private void setProxyUrl(final String proxyUrl) {

        // TODO: here are two configuration objects which have to be unified or correctly separated
        config.communicationProxyUrl = proxyUrl;
        if (elasticConfig != null) elasticConfig.communicationProxyUrl = proxyUrl;
    }


    private Integer getBusCount(final XPathService communication) throws Exception {
        return communication.countNodes("/communication/client/connections/server");
    }
}
