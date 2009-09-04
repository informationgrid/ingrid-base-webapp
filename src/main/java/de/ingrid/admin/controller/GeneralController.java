                    package de.ingrid.admin.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.object.IDataType;
import de.ingrid.admin.object.Partner;
import de.ingrid.admin.object.Provider;
import de.ingrid.admin.service.CommunicationInterface;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;

@Controller
@RequestMapping(value = "/base/general.html")
@SessionAttributes("plugDescription")
public class GeneralController {

    private final CommunicationInterface _communicationInterface;
    private final IDataType[] _dataTypes;

    @Autowired
    public GeneralController(CommunicationInterface communicationInterface, IDataType... dataTypes) throws Exception {
        _communicationInterface = communicationInterface;
        _dataTypes = dataTypes;
    }
    
    @ModelAttribute("partners")
    public List<Partner> getPartners() throws Exception {
        List<Partner> list = new ArrayList<Partner>();
        IBus bus = _communicationInterface.getIBus();
        IngridQuery ingridQuery = new IngridQuery();
        ingridQuery.addField(new FieldQuery(false, false, "datatype", "management"));
        ingridQuery.addField(new FieldQuery(false, false, "management_request_type", "1"));

        IngridHits hits = bus.search(ingridQuery, 1000, 0, 0, 120000);
        if (hits.length() > 0) {
            ArrayList partners = hits.getHits()[0].getArrayList("partner");
            for (Object object : partners) {
                Map<String, Object> map = (Map<String, Object>) object;
                String partnerName = (String) map.get("name");
                String partnerId = (String) map.get("partnerid");
                list.add(new Partner(partnerId, partnerName));
            }
        }

        return list;
    }

    @ModelAttribute("providers")
    public List<Provider> getProviders() throws Exception {
        List<Provider> list = new ArrayList<Provider>();
        IBus bus = _communicationInterface.getIBus();
        IngridQuery ingridQuery = new IngridQuery();
        ingridQuery.addField(new FieldQuery(false, false, "datatype", "management"));
        ingridQuery.addField(new FieldQuery(false, false, "management_request_type", "2"));

        IngridHits hits = bus.search(ingridQuery, 1000, 0, 0, 120000);
        if (hits.length() > 0) {
            ArrayList providers = hits.getHits()[0].getArrayList("provider");
            for (Object object : providers) {
                Map<String, Object> map = (Map<String, Object>) object;
                String providerName = (String) map.get("name");
                String providerId = (String) map.get("providerid");
                list.add(new Provider(providerId, providerName));
            }
        }

        return list;
    }
    
    @ModelAttribute("datatypes")
    public IDataType[] injectDataTypes() {
        return _dataTypes;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String getGeneral(final ModelMap modelMap, @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject, @ModelAttribute("partners") final List<Partner> partners, @ModelAttribute("providers") final List<Provider> providers) {
    	final SortedMap<String, List<Provider>> map = new TreeMap<String, List<Provider>>();
    	for(final Partner pa : partners) {
    		final List<Provider> list = new ArrayList<Provider>();
    		final Iterator<Provider> it = providers.iterator();
    		if(pa.getShortName().equals("bund")) {
    			while(it.hasNext()) {
    				final Provider pr = it.next();
	    			if(pr.getShortName().startsWith("bu")) {
	    				list.add(pr);
	    				it.remove();
	    			}
	    		}
    		} else {
    			while(it.hasNext()) {
    				final Provider pr = it.next();
	    			if(pr.getShortName().startsWith(pa.getShortName())) {
	    				list.add(pr);
	    				it.remove();
	    			}
	    		}
    		}
    		map.put(pa.getShortName(), list);
    	}
    	modelMap.addAttribute("jsonMap", toJsonMap(map));
    	
        return "/base/general";
    }

    private String toJsonMap(final SortedMap<String, List<Provider>> map) {
    	final StringBuilder result = new StringBuilder("{");
    	for(final String key : map.keySet()) {
    		result.append("'" + key + "':[");
    		Iterator<Provider> it = map.get(key).iterator();
    		while(it.hasNext()) {
    			final Provider p = it.next();
    			result.append("{'shortName':'" + p.getShortName() + "','displayName':'" + p.getDisplayName() + "'}");
    			if(it.hasNext()) {
    				result.append(",");
    			}
    		}
    		result.append("]");
    		if(key != map.lastKey()) {
    			result.append(",");
    		}
    	}
    	result.append("}");
    	return result.toString();
    }
    
    @RequestMapping(method = RequestMethod.POST)
    public String postGeneral(@ModelAttribute("plugDescription") PlugdescriptionCommandObject commandObject) {
        // TODO validate
		// return "redirect:/base/finishBase.html";
		return "redirect:/base/partner.html";
    }

}
