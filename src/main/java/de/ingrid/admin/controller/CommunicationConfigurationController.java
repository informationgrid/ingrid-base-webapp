package de.ingrid.admin.controller;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.weta.components.communication.configuration.XPathService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.ingrid.admin.command.CommunicationCommandObject;
import de.ingrid.admin.service.CommunicationInterface;

@Controller
@RequestMapping(value = "/base/communication.html")
public class CommunicationConfigurationController {

    private final CommunicationInterface _communicationInterface;

    @Autowired
    public CommunicationConfigurationController(final CommunicationInterface communicationInterface) {
        _communicationInterface = communicationInterface;
    }

    @ModelAttribute("communication")
    public CommunicationCommandObject createCommandObject() throws Exception {
        // create command object
        final CommunicationCommandObject commandObject = new CommunicationCommandObject();

        // create the xpath
        final XPathService communication = new XPathService();

        // open communication file
        final File communicationFile = _communicationInterface.getCommunicationFile();
        // if it exists save the proxy url
        if (communicationFile.exists()) {
            communication.registerDocument(communicationFile);
        } else {
            // open template file
            final InputStream inputStream = CommunicationConfigurationController.class
                    .getResourceAsStream("/communication-template.xml");
            communication.registerDocument(inputStream);
        }
        // only set default proxy url if it's not already set
        if (commandObject.getProxyServiceUrl() == null) {
            commandObject.setProxyServiceUrl(communication.parseAttribute("/communication/client", "name"));
        }
        // save default values
        commandObject.setBusProxyServiceUrl(communication.parseAttribute("/communication/client/connections/server",
                "name"));
        commandObject.setIp(communication.parseAttribute("/communication/client/connections/server/socket", "ip"));
        commandObject.setPort(Integer.parseInt(communication.parseAttribute(
                "/communication/client/connections/server/socket", "port")));

        // return command object
        return commandObject;
    }

    @ModelAttribute("busses")
    public List<CommunicationCommandObject> existingBusses() throws Exception {
        // open communication file
        final File communicationFile = _communicationInterface.getCommunicationFile();
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
        return busses;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String getCommunication(final ModelMap modelMap,
            @ModelAttribute("communication") final CommunicationCommandObject commandObject,
            @ModelAttribute("busses") final List<CommunicationCommandObject> busses,
            @RequestParam(value = "action", required = false) final String action,
            @RequestParam(value = "bus", required = false) final Integer busIndex) throws Exception {
        if (action != null) {
            // open communication file
            final XPathService communication = new XPathService();
            final File communicationFile = _communicationInterface.getCommunicationFile();
            communication.registerDocument(communicationFile);

            // find out which action is chosen
            if ("delete".equals(action)) {
                if (busIndex != null && busses.size() > busIndex) {
                    // delete from communication
                    communication.removeNode("/communication/client/connections/server", busIndex);
                    communication.store(communicationFile);
                    // delete from model attribute
                    busses.remove((int) busIndex);
                    modelMap.addAttribute("busses", busses);
                }
            } else if ("edit".equals(action)) {
                // update command object
                modelMap.addAttribute("bus", busIndex);
                commandObject.setBusProxyServiceUrl(communication.parseAttribute(
                        "/communication/client/connections/server", "name", busIndex));
                commandObject.setIp(communication.parseAttribute("/communication/client/connections/server/socket",
                        "ip", busIndex));
                commandObject.setPort(Integer.parseInt(communication.parseAttribute(
                        "/communication/client/connections/server/socket", "port", busIndex)));
            }
        }
        return "/base/communication";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String postCommunication(final ModelMap modelMap,
            @ModelAttribute("communication") CommunicationCommandObject commandObject,
            @ModelAttribute("busses") List<CommunicationCommandObject> busses,
            @RequestParam(value = "form", required = false) final String form,
            @RequestParam(value = "bus", required = false) Integer busIndex)
            throws Exception {
        // TODO: validate that busUrls are unique

        // create directories if necessary
        final File communicationFile = _communicationInterface.getCommunicationFile();
        if(communicationFile.getParentFile() != null) {
            if (!communicationFile.getParentFile().exists()) {
                communicationFile.getParentFile().mkdirs();
            }
        }

        // open xml document or file
        final XPathService communication = new XPathService();
        if (!communicationFile.exists()) {
            final InputStream inputStream = CommunicationConfigurationController.class
                    .getResourceAsStream("/communication-template.xml");
            communication.registerDocument(inputStream);
        } else {
            communication.registerDocument(communicationFile);
        }

        // maybe a bus have to be update
        if (busIndex == null) {
            busIndex = 0;
        }

        // update client url
        if ("base".equals(form)) {
            // update client name
            communication.setAttribute("/communication/client", "name", commandObject.getProxyServiceUrl());

            // update base bus
            communication.setAttribute("/communication/client/connections/server", "name", commandObject
                    .getBusProxyServiceUrl(), busIndex);
            communication.setAttribute("/communication/client/connections/server/socket", "port", ""
                    + commandObject.getPort(), busIndex);
            communication.setAttribute("/communication/client/connections/server/socket", "ip", commandObject.getIp(),
                    busIndex);

            // if the base bus already exist delete it from the view
            if ((busses != null) && (busses.size() > busIndex)) {
                busses.remove((int) busIndex);
            }
        } else {
            // determine count of ibusses
            final int count = communication.countNodes("/communication/client/connections/server");
            busIndex = count;

            // create default nodes and attributes
            communication.addNode("/communication/client/connections", "server");
            communication.addNode("/communication/client/connections/server", "socket", count);
            communication.addAttribute("/communication/client/connections/server/socket", "timeout", "10", count);
            communication.addNode("/communication/client/connections/server", "messages", count);
            communication.addAttribute("/communication/client/connections/server/messages", "maximumSize", "1048576",
                    count);
            communication
                    .addAttribute("/communication/client/connections/server/messages", "threadCount", "100", count);

            // add a new bus
            communication.addAttribute("/communication/client/connections/server", "name", commandObject
                    .getBusProxyServiceUrl(), count);
            communication.addAttribute("/communication/client/connections/server/socket", "port", ""
                    + commandObject.getPort(), count);
            communication.addAttribute("/communication/client/connections/server/socket", "ip", commandObject.getIp(),
                    count);

        }
        // add bus to the view
        if (busses == null) {
            busses = new ArrayList<CommunicationCommandObject>();
        }
        busses.add(busIndex, commandObject);
        modelMap.addAttribute("busses", busses);

        // save the new data
        communication.store(communicationFile);

        // update command object
        commandObject = createCommandObject();
        modelMap.addAttribute("communication", commandObject);

        if ("base".equals(form)) {
            // restart communication interface
            _communicationInterface.restart();
        }

        return "/base/communication";
    }

}
