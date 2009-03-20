package de.ingrid.admin;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import net.weta.components.communication.configuration.XPathService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/base/communication.html")
public class CommunicationConfigurationController {

    @ModelAttribute("communication")
    public CommunicationCommandObject createCommandObject() throws Exception {
        CommunicationCommandObject commandObject = new CommunicationCommandObject();
        InputStream inputStream = CommunicationConfigurationController.class.getResourceAsStream("/communication.xml");
        XPathService pathService = new XPathService();
        pathService.registerDocument(inputStream);
        String clientName = pathService.parseAttribute("/communication/client", "name", 0);
        String serverName = pathService.parseAttribute("/communication/client/connections/server", "name", 0);
        String serverPort = pathService.parseAttribute("/communication/client/connections/server/socket", "port", 0);
        String serverIp = pathService.parseAttribute("/communication/client/connections/server/socket", "ip", 0);
        commandObject.setBusProxyServiceUrl(serverName);
        commandObject.setIp(serverIp);
        commandObject.setPort(Integer.parseInt(serverPort));
        commandObject.setProxyServiceUrl(clientName);
        return commandObject;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String getCommunication() {
        return "/base/communication";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String postCommunication(@ModelAttribute("communication") CommunicationCommandObject commandObject)
            throws Exception {
        URL url = CommunicationConfigurationController.class.getResource("/communication.xml");
        String fileString = url.getFile();
        XPathService pathService = new XPathService();
        pathService.registerDocument(new File(fileString));
        pathService.setAttribute("/communication/client", "name", commandObject.getProxyServiceUrl(), 0);
        pathService.setAttribute("/communication/client/connections/server", "name", commandObject
                .getBusProxyServiceUrl(), 0);
        pathService.setAttribute("/communication/client/connections/server/socket", "port", ""
                + commandObject.getPort(), 0);
        pathService.setAttribute("/communication/client/connections/server/socket", "ip", commandObject.getIp(), 0);
        pathService.store(new File(fileString));
        return "redirect:/base/welcome.html";
    }

}
