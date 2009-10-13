package de.ingrid.admin.validation;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import de.ingrid.admin.command.PlugdescriptionCommandObject;

@Service
public class PlugDescValidator extends AbstractValidator<PlugdescriptionCommandObject> {

    public final Errors validateWorkingDir(final Errors errors) {
        rejectIfEmptyOrWhitespace(errors, "workinDirectory");
        return errors;
    }

    public final Errors validateGeneral(final Errors errors) {
        rejectIfEmptyOrWhitespace(errors, "organisation");
        rejectIfEmptyOrWhitespace(errors, "organisationAbbr");

        rejectIfEmptyOrWhitespace(errors, "personSureName");
        rejectIfEmptyOrWhitespace(errors, "personName");
        rejectIfEmptyOrWhitespace(errors, "personPhone");
        rejectIfEmptyOrWhitespace(errors, "personMail");

        rejectIfEmptyOrWhitespace(errors, "dataSourceName");
        rejectIfNullOrEmpty(errors, "dataTypes");

        rejectIfEmptyOrWhitespace(errors, "proxyServiceURL");

        rejectIfEmptyOrWhitespace(errors, "iplugAdminGuiUrl");
        rejectIfEmptyOrWhitespace(errors, "iplugAdminGuiPort");
        rejectIfEmptyOrWhitespace(errors, "iplugAdminPassword");

        return errors;
    }

    public final Errors validatePartners(final Errors errors) {
        rejectIfNullOrEmpty(errors, "partners");
        return errors;
    }

    public final Errors validateProviders(final Errors errors) {
        rejectIfNullOrEmpty(errors, "providers");
        return errors;
    }
}
