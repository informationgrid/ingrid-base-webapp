package de.ingrid.admin.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;
import de.ingrid.admin.Utils;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.object.IDataType;
import de.ingrid.admin.object.Partner;
import de.ingrid.admin.object.Provider;
import de.ingrid.admin.service.CommunicationService;
import de.ingrid.admin.validation.PlugDescValidator;

@Controller
@SessionAttributes("plugDescription")
public class GeneralController extends AbstractController {

    private final CommunicationService _communicationInterface;

    private final IDataType[] _dataTypes;

    private final PlugDescValidator _validator;

    @Autowired
    public GeneralController(final CommunicationService communicationInterface, final PlugDescValidator validator,
            final IDataType... dataTypes) throws Exception {
        _communicationInterface = communicationInterface;
        _validator = validator;
        _dataTypes = dataTypes;
    }

    @ModelAttribute("partners")
    public List<Partner> getPartners() throws Exception {
        return Utils.getPartners(_communicationInterface.getIBus());
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
        // create map
        final SortedMap<String, List<Provider>> map = createPartnerProviderMap(partners, getProviders());
        modelMap.addAttribute("jsonMap", toJsonMap(map));

        return IViews.GENERAL;
    }

    @RequestMapping(value = IUris.GENERAL, method = RequestMethod.POST)
    public String postGeneral(final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject, final Errors errors,
            @ModelAttribute("partners") final List<Partner> partners) throws Exception {
        // set up proxy service url
        commandObject.setProxyServiceURL(_communicationInterface.getPeerName());

        if (_validator.validateGeneral(errors).hasErrors()) {
            return getGeneral(modelMap, commandObject, errors, partners);
        }

        // add data type includes
        commandObject.addIncludedDataTypes(_dataTypes);

        return redirect(IUris.PARTNER);
    }

    private List<Provider> getProviders() throws Exception {
        return Utils.getProviders(_communicationInterface.getIBus());
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
