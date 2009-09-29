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
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.object.IDataType;
import de.ingrid.admin.object.Partner;
import de.ingrid.admin.object.Provider;
import de.ingrid.admin.service.CommunicationInterface;
import de.ingrid.admin.validation.PlugDescValidator;
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

    private final PlugDescValidator _validator;

    @Autowired
    public GeneralController(final CommunicationInterface communicationInterface, final PlugDescValidator validator,
            final IDataType... dataTypes) throws Exception {
        _communicationInterface = communicationInterface;
        _validator = validator;
        _dataTypes = dataTypes;
    }

    @ModelAttribute("partners")
    public List<Partner> getPartners() throws Exception {
        final List<Partner> list = new ArrayList<Partner>();
        final IBus bus = _communicationInterface.getIBus();
        final IngridQuery ingridQuery = new IngridQuery();
        ingridQuery.addField(new FieldQuery(false, false, "datatype", "management"));
        ingridQuery.addField(new FieldQuery(false, false, "management_request_type", "1"));

        final IngridHits hits = bus.search(ingridQuery, 1000, 0, 0, 120000);
        if (hits.length() > 0) {
            final ArrayList partners = hits.getHits()[0].getArrayList("partner");
            for (final Object object : partners) {
                final Map<String, Object> map = (Map<String, Object>) object;
                final String partnerName = (String) map.get("name");
                final String partnerId = (String) map.get("partnerid");
                list.add(new Partner(partnerId, partnerName));
            }
        }

        return list;
    }

    @ModelAttribute("datatypes")
    public IDataType[] injectDataTypes() {
        return _dataTypes;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String getGeneral(final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject, final Errors errors,
            @ModelAttribute("partners") final List<Partner> partners) throws Exception {
        final SortedMap<String, List<Provider>> map = createPartnerProviderMap(partners, getProviders());
        modelMap.addAttribute("jsonMap", toJsonMap(map));

        return "/base/general";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String postGeneral(final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject, final Errors errors,
            @ModelAttribute("partners") final List<Partner> partners) throws Exception {
        if (_validator.validateGeneral(errors).hasErrors()) {
            return getGeneral(modelMap, commandObject, errors, partners);
        }
        return "redirect:/base/partner.html";
    }

    private List<Provider> getProviders() throws Exception {
        final List<Provider> list = new ArrayList<Provider>();
        final IBus bus = _communicationInterface.getIBus();
        final IngridQuery ingridQuery = new IngridQuery();
        ingridQuery.addField(new FieldQuery(false, false, "datatype", "management"));
        ingridQuery.addField(new FieldQuery(false, false, "management_request_type", "2"));

        final IngridHits hits = bus.search(ingridQuery, 1000, 0, 0, 120000);
        if (hits.length() > 0) {
            final ArrayList providers = hits.getHits()[0].getArrayList("provider");
            for (final Object object : providers) {
                final Map<String, Object> map = (Map<String, Object>) object;
                final String providerName = (String) map.get("name");
                final String providerId = (String) map.get("providerid");
                list.add(new Provider(providerId, providerName));
            }
        }

        return list;
    }

    private final SortedMap<String, List<Provider>> createPartnerProviderMap(final List<Partner> partners,
            final List<Provider> providers) {
        final SortedMap<String, List<Provider>> map = new TreeMap<String, List<Provider>>();
        for (final Partner pa : partners) {
            final List<Provider> list = new ArrayList<Provider>();
            final Iterator<Provider> it = providers.iterator();
            if (pa.getShortName().equals("bund")) {
                while (it.hasNext()) {
                    final Provider pr = it.next();
                    if (pr.getShortName().startsWith("bu")) {
                        list.add(pr);
                        it.remove();
                    }
                }
            } else {
                while (it.hasNext()) {
                    final Provider pr = it.next();
                    if (pr.getShortName().startsWith(pa.getShortName())) {
                        list.add(pr);
                        it.remove();
                    }
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
}
