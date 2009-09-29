package de.ingrid.admin.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.StringUtils;
import de.ingrid.admin.Utils;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.object.Partner;
import de.ingrid.admin.service.CommunicationInterface;
import de.ingrid.admin.validation.IErrorKeys;
import de.ingrid.admin.validation.PlugDescValidator;

@Controller
@RequestMapping(value = "/base/partner.html")
@SessionAttributes("plugDescription")
public class PartnerController {

	private final CommunicationInterface _communicationInterface;

    private final PlugDescValidator _validator;

	@Autowired
    public PartnerController(final CommunicationInterface communicationInterface, final PlugDescValidator validator)
			throws Exception {
		_communicationInterface = communicationInterface;
        _validator = validator;
	}

	@ModelAttribute("partnerList")
	public List<Partner> getPartners() throws Exception {
        return Utils.getPartners(_communicationInterface.getIBus());
	}

	@RequestMapping(method = RequestMethod.GET)
    public String getPartner(final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject,
            @ModelAttribute("partnerList") final List<Partner> partnerList) {

        final List<Partner> partners = new ArrayList<Partner>();
        for (final String shortName : commandObject.getPartners()) {
            partners.add(getByShortName(partnerList, shortName));
        }
        modelMap.addAttribute("partners", partners);

		return "/base/partner";
	}

	@RequestMapping(method = RequestMethod.POST)
    public String postPartner(final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject, final Errors errors,
            @ModelAttribute("partnerList") final List<Partner> partnerList,
            @RequestParam("action") final String action,
            @RequestParam(value = "partner", required = false) final String partner,
            @RequestParam(value = "id", required = false) final String id) {

        if ("add".equals(action)) {
            if (StringUtils.isEmptyOrWhiteSpace(partner)) {
                _validator.rejectError(errors, "partners", IErrorKeys.EMPTY);
            } else {
                commandObject.addPartner(partner);
            }
        } else if ("delete".equals(action)) {
            commandObject.removePartner(id);
        } else if ("submit".equals(action)) {
            if (!_validator.validatePartners(errors).hasErrors()) {
                return "redirect:/base/provider.html";
            }
        }

        return getPartner(modelMap, commandObject, partnerList);
	}

    private final Partner getByShortName(final List<Partner> partners, final String shortName) {
        for (final Partner partner : partners) {
            if (partner.getShortName().equals(shortName)) {
                return partner;
            }
        }
        return null;
    }
}
