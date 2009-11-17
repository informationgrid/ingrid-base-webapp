package de.ingrid.admin.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import net.weta.components.communication.configuration.XPathService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.IUris;
import de.ingrid.admin.IViews;
import de.ingrid.admin.command.FieldQueryCommandObject;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.service.CommunicationService;
import de.ingrid.admin.validation.FieldQueryValidator;
import de.ingrid.utils.QueryExtension;
import de.ingrid.utils.QueryExtensionContainer;
import de.ingrid.utils.query.FieldQuery;

@Controller
@SessionAttributes("plugDescription")
public class FieldQueryController extends AbstractController {

    private final CommunicationService _communicationInterface;

    private final FieldQueryValidator _validator;

    @Autowired
    public FieldQueryController(final CommunicationService communicationInterface, final FieldQueryValidator validator) {
        _communicationInterface = communicationInterface;
        _validator = validator;
    }

    @ModelAttribute("busUrls")
    public final String[] getBusUrls() throws Exception {
        // open the communication file
        final XPathService pathService = new XPathService();
        pathService.registerDocument(_communicationInterface.getCommunicationFile());
        // count number of ibuses
        final int count = pathService.countNodes("/communication/client/connections/server");
        final String[] busUrls = new String[count];
        // add all ibus names
        for (int i = 0; i < count; i++) {
            busUrls[i] = pathService.parseAttribute("/communication/client/connections/server", "name", i);
        }

        return busUrls;
    }

    @ModelAttribute("fieldQuery")
    public final FieldQueryCommandObject getCommandObject() {
        return new FieldQueryCommandObject();
    }

    @RequestMapping(value = IUris.FIELD_QUERY, method = RequestMethod.GET)
    public String getFieldQuery(final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject) throws Exception {

        // catching all field queries together in a list of maps
        final List<FieldQueryCommandObject> fields = getFields(commandObject.getQueryExtensions());
        modelMap.addAttribute("fields", fields);

        return IViews.FIELD_QUERY;
	}

    @RequestMapping(value = IUris.FIELD_QUERY, method = RequestMethod.POST)
    public String postFieldQuery(final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject,
            @ModelAttribute("fieldQuery") final FieldQueryCommandObject fieldQuery, final Errors errors,
            @RequestParam("action") final String action, @RequestParam(value = "id", required = false) final Integer id)
            throws Exception {
        if ("add".equals(action)) {
            if (!_validator.validate(errors).hasErrors()) {
                addFieldQuery(commandObject, fieldQuery);
            }
        } else if ("delete".equals(action)) {
            final FieldQueryCommandObject field = getFields(commandObject.getQueryExtensions()).get(id);
            deleteFieldQuery(commandObject, field);
        } else if ("submit".equals(action)) {
            // redirect to the first page of iPlug specific data
            return redirect(IUris.EXTRAS);
        }

        return getFieldQuery(modelMap, commandObject);
	}

    private List<FieldQueryCommandObject> getFields(final Map<String, QueryExtension> extensions) {
        final List<FieldQueryCommandObject> fields = new ArrayList<FieldQueryCommandObject>();
        if (extensions != null) {
            for (final String key : extensions.keySet()) {
                final QueryExtension extension = extensions.get(key);
                final Set<Pattern> patterns = extension.getPatterns();
                if (patterns != null) {
                    for (final Pattern pattern : patterns) {
                        for (final FieldQuery fieldQuery : extension.getFieldQueries(pattern)) {
                            final FieldQueryCommandObject fq = new FieldQueryCommandObject();
                            fq.setBusUrl(key);
                            fq.setRegex(pattern.pattern());
                            fq.setKey(fieldQuery.getFieldName());
                            fq.setValue(fieldQuery.getFieldValue());
                            if (fieldQuery.isProhibited()) {
                                fq.setProhibited();
                            } else if (fieldQuery.isRequred()) {
                                fq.setRequired();
                            }
                            fields.add(fq);
                        }
                    }
                }
            }
        }
        return fields;
    }

    private void addFieldQuery(final PlugdescriptionCommandObject commandObject,
            final FieldQueryCommandObject fieldQuery) {
        // get container
        QueryExtensionContainer container = commandObject.getQueryExtensionContainer();
        if (null == container) {
            // create container
            container = new QueryExtensionContainer();
            commandObject.setQueryExtensionContainer(container);
        }
        // get extension
        QueryExtension extension = container.getQueryExtension(fieldQuery.getBusUrl());
        if (null == extension) {
            // create extension
            extension = new QueryExtension();
            extension.setBusUrl(fieldQuery.getBusUrl());
            container.addQueryExtension(extension);
        }
        // create field query
        final Pattern pattern = Pattern.compile(fieldQuery.getRegex());
        final FieldQuery fq = new FieldQuery(fieldQuery.getRequired(), fieldQuery.getProhibited(), fieldQuery.getKey(),
                fieldQuery.getValue());
        extension.addFieldQuery(pattern, fq);
    }

    private void deleteFieldQuery(final PlugdescriptionCommandObject commandObject,
            final FieldQueryCommandObject fieldQuery) {
        // get extension
        final QueryExtension extension = commandObject.getQueryExtensionContainer().getQueryExtension(fieldQuery.getBusUrl());
        // get patterns for field queries with equal pattern
        final Set<Pattern> patterns = extension.getPatterns();
        Set<FieldQuery> fieldQueries = null;
        for (final Pattern pattern : patterns) {
            if (pattern.pattern() == fieldQuery.getRegex()) {
                fieldQueries = extension.getFieldQueries(pattern);
                break;
            }
        }
        // find correct field query
        for (final FieldQuery fq : fieldQueries) {
            if (fieldQuery.getKey().equals(fq.getFieldName()) && fieldQuery.getValue().equals(fq.getFieldValue())
                    && fieldQuery.getProhibited() == fq.isProhibited() && fieldQuery.getRequired() == fq.isRequred()) {
                // delete it
                fieldQueries.remove(fq);
                break;
            }
        }
    }
}
