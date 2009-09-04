package de.ingrid.admin.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.object.Partner;
import de.ingrid.admin.service.CommunicationInterface;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;

@Controller
@RequestMapping(value = "/base/partner.html")
@SessionAttributes("plugDescription")
public class PartnerController {

	private final CommunicationInterface _communicationInterface;

	@Autowired
	public PartnerController(final CommunicationInterface communicationInterface)
			throws Exception {
		_communicationInterface = communicationInterface;
	}

	@ModelAttribute("partnerList")
	public List<Partner> getPartners() throws Exception {
		final List<Partner> list = new ArrayList<Partner>();
		final IBus bus = _communicationInterface.getIBus();
		final IngridQuery ingridQuery = new IngridQuery();
		ingridQuery.addField(new FieldQuery(false, false, "datatype",
				"management"));
		ingridQuery.addField(new FieldQuery(false, false,
				"management_request_type", "1"));

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

	@RequestMapping(method = RequestMethod.GET)
	public String getPartner(final ModelMap modelMap, @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject) {
		return "/base/partner";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String postPartner(@ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject,
			@ModelAttribute("partnerList") final List<Partner> partnerList, @RequestParam(value = "partners", required = false) final String[] partners) {
		final String[] currentPartners = commandObject.getPartners();
		if (currentPartners != null) {
			commandObject.remove(PlugDescription.PARTNER);
		}
		if (partners != null) {
			for (final String partner : partners) {
				commandObject.addPartner(partner);
			}
		}

		return "redirect:/base/fieldQuery.html";
	}
}
