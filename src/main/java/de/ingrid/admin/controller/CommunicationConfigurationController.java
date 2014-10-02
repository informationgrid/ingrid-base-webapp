package de.ingrid.admin.controller;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import net.weta.components.communication.configuration.XPathService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;
import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.command.CommunicationCommandObject;
import de.ingrid.admin.service.CommunicationService;
import de.ingrid.admin.validation.CommunicationValidator;
import de.ingrid.admin.validation.IErrorKeys;

@Controller
public class CommunicationConfigurationController extends AbstractController {

    public static final int DEFAULT_TIMEOUT = 10;

    public static final int DEFAULT_MAXIMUM_SIZE = 1048576;

    public static final int DEFAULT_THREAD_COUNT = 100;

    private String _defaultProxyServiceUrl = "";

    private final CommunicationService _communicationService;

    private final CommunicationValidator _validator;

    @Autowired
    public CommunicationConfigurationController(final CommunicationService communicationService,
            final CommunicationValidator validator) {
        _communicationService = communicationService;
        _validator = validator;
    }

    @ModelAttribute("communication")
    public CommunicationCommandObject createCommandObject() throws Exception {
        // create command object
        final CommunicationCommandObject commandObject = new CommunicationCommandObject();

        // create the xpath
        final XPathService communication = new XPathService();

        // open communication file
        final File communicationFile = _communicationService.getCommunicationFile();

        // bus count
        Integer count = 0;

        if (communicationFile.exists()) {
            // only save proxy service url
            communication.registerDocument(communicationFile);
            commandObject.setProxyServiceUrl(communication.parseAttribute("/communication/client", "name"));
            count = getBusCount(communication);
        } else {
            commandObject.setProxyServiceUrl( JettyStarter.getInstance().config.communicationProxyUrl );
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
//        String proxyServiceUrl = commandObject.getProxyServiceUrl();
//        final String userName = System.getProperty("user.name");
//        proxyServiceUrl = proxyServiceUrl.endsWith("_" + userName) ? proxyServiceUrl : proxyServiceUrl + "_" + userName;
//        commandObject.setProxyServiceUrl(proxyServiceUrl);

        // return command object
        return commandObject;
    }

    @ModelAttribute("busses")
    public List<CommunicationCommandObject> existingBusses() throws Exception {
        // open communication file
        /*final File communicationFile = _communicationService.getCommunicationFile();
        if (!communicationFile.exists()) {
            return null;
        }
        // create xpath service for xml
        final XPathService communication = new XPathService();
        communication.registerDocument(communicationFile);
        // determine count of ibusses
        final int count = communication.countNodes("/communication/client/connections/server");
        // create List of communication
        final List<CommunicationCommandObject> busses = new ArrayList<CommunicationCommandObject>();
        // and get all information about each ibus
        for (int i = 0; i < count; i++) {
            final CommunicationCommandObject bus = new CommunicationCommandObject();
            bus.setBusProxyServiceUrl(communication.parseAttribute("/communication/client/connections/server", "name",
                    i));
            bus.setIp(communication.parseAttribute("/communication/client/connections/server/socket", "ip", i));
            bus.setPort(Integer.parseInt(communication.parseAttribute(
                    "/communication/client/connections/server/socket", "port", i)));
            busses.add(bus);
        }
        // return all busses
        return busses;*/
        List<CommunicationCommandObject> ibusses = JettyStarter.getInstance().config.ibusses;
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
                setProxyUrl(communication, commandObject.getProxyServiceUrl());

                if (!communicationFile.exists() || getBusCount(communication) == 0) {
                    tryToAdd = true;
                }
            }

            if ("add".equals(action) || tryToAdd) {
                // set proxy url
                if (!_validator.validateProxyUrl(errors, _defaultProxyServiceUrl).hasErrors()) {
                    setProxyUrl(communication, commandObject.getProxyServiceUrl());
                }
                // add new bus
                if (_validator.validateBus(errors).hasErrors()) {
                    return IViews.COMMUNICATION;
                }
                
                JettyStarter.getInstance().config.ibusses.add( commandObject );

            } else if ("delete".equals(action)) {
                // delete bus
                JettyStarter.getInstance().config.ibusses.remove( id.intValue() );
            } else if ("set".equals(action)) {
                // set base bus
                CommunicationCommandObject newDefault = JettyStarter.getInstance().config.ibusses.remove( id.intValue() );
                JettyStarter.getInstance().config.ibusses.add( 0, newDefault );
            }

            // save the new data
            JettyStarter.getInstance().config.writeCommunication();
            JettyStarter.getInstance().config.writeCommunicationToProperties();

            // when busses have been switched we need to restart communication with new config-file
            if ("set".equals(action) || "add".equals(action)) {
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
            }
        }

        return redirect( IViews.COMMUNICATION + ".html" );
    }

    private final XPathService openCommunication(final File communicationFile) throws Exception {
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

    private void setProxyUrl(final XPathService communication, final String proxyUrl) throws Exception {
        //communication.setAttribute("/communication/client", "name", proxyUrl);
        JettyStarter.getInstance().config.communicationProxyUrl = proxyUrl;
    }


    private Integer getBusCount(final XPathService communication) throws Exception {
        return communication.countNodes("/communication/client/connections/server");
    }
}
