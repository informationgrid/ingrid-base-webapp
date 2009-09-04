package de.ingrid.admin.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import net.weta.components.communication.configuration.XPathService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.service.CommunicationInterface;
import de.ingrid.utils.QueryExtension;
import de.ingrid.utils.QueryExtensionContainer;
import de.ingrid.utils.query.FieldQuery;

@Controller
@RequestMapping(value = "/base/fieldQuery.html")
@SessionAttributes("plugDescription")
public class FieldQueryController {

    private final CommunicationInterface _communicationInterface;

    @Autowired
    public FieldQueryController(final CommunicationInterface communicationInterface) {
        _communicationInterface = communicationInterface;
    }

	@RequestMapping(method = RequestMethod.GET)
    public String getFieldQuery(final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject) throws Exception {

        // catching all field queries together in a list of maps
	    final List<Map<String, String>> fields = new ArrayList<Map<String, String>>();
        // getting all extensions
	    final Map<String, QueryExtension> extensions = commandObject.getQueryExtensions();
	    if(extensions != null) {
	        for(final String key : extensions.keySet()) {
	            final QueryExtension extension = extensions.get(key);
	            final Set<Pattern> patterns = extension.getPatterns();
                // run through all pattern of each extension
	            if(patterns != null) {
                    for (final Pattern pattern : patterns) {
                        // create for every field query a new map and store them
                        // in "fields"
                        for (final FieldQuery fieldQuery : extension.getFieldQueries(pattern)) {
                            final Map<String, String> map = new HashMap<String, String>();
                            map.put("bus_url", key);
                            map.put("regex", pattern.pattern());
                            map.put("key", fieldQuery.getFieldName());
                            map.put("value", fieldQuery.getFieldValue());
                            map.put("prohibited", fieldQuery.isProhibited() ? "true" : "false");
                            map.put("required", fieldQuery.isRequred() ? "true" : "false");
                            fields.add(map);
                        }
                    }
	            }
	        }
	    }
        // that's all :)
        // now give it to the view
        modelMap.addAttribute("fields", fields);

        // now it's time for the bus url's
        // so open up the communication file
        final XPathService pathService = new XPathService();
        pathService.registerDocument(_communicationInterface.getCommunicationFile());
        // count number of ibuses
        final int count = pathService.countNodes("/communication/client/connections/server");
        final String[] busUrls = new String[count];
        // add all ibus names
        for (int i = 0; i < count; i++) {
            busUrls[i] = pathService.parseAttribute("/communication/client/connections/server", "name", i);
        }
        // and put it into the view
        modelMap.addAttribute("busUrls", busUrls);

		return "/base/fieldQuery";
	}

	@RequestMapping(method = RequestMethod.POST)
    public String postFieldQuery(@ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject,
            @RequestParam(value = "fieldQuery", required = false) final String[] fieldQueries) {
		// creating new container
        final QueryExtensionContainer container = new QueryExtensionContainer();
		if (null != fieldQueries) {
            for (final String s : fieldQueries) {
                // 0 = bus_url; 1 = regex; 2 = key; 3 = value;
                // 4 = prohibited; 5 = required
                final String[] split = s.split(";");
                // creating new / getting extension
                QueryExtension extension = container.getQueryExtension(split[0]);
                if (extension == null) {
                    extension = new QueryExtension();
                    extension.setBusUrl(split[0]);
                }
				extension.addFieldQuery(split[1], new FieldQuery(Boolean.parseBoolean(split[5]), Boolean.parseBoolean(split[4]), split[2], split[3]));
				// add extension to container
				container.addQueryExtension(extension);
			}
		}
		// add container to plug
		commandObject.setQueryExtensionContainer(container);

        // redirect to step 2
        return "redirect:/iplug/welcome.html";
	}
}
