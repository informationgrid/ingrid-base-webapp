package de.ingrid.admin.validation;

import java.net.InetAddress;
import java.net.Socket;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import de.ingrid.admin.IKeys;
import de.ingrid.admin.command.PlugdescriptionCommandObject;

@Service
public class PlugDescValidator extends AbstractValidator<PlugdescriptionCommandObject> {

    public final Errors validateWorkingDir(final Errors errors) {
        rejectIfEmptyOrWhitespace(errors, "workinDirectory");
        return errors;
    }

    public final Errors validateGeneral(final Errors errors, final boolean hasBusConnection) {
        if (hasBusConnection) {
            rejectIfEmptyOrWhitespace(errors, "organisation");
            rejectIfEmptyOrWhitespace(errors, "organisationAbbr");
        }
        rejectIfEmptyOrWhitespace(errors, "organisationPartnerAbbr");

        rejectIfEmptyOrWhitespace(errors, "personSureName");
        rejectIfEmptyOrWhitespace(errors, "personName");
        rejectIfEmptyOrWhitespace(errors, "personPhone");
        rejectIfEmptyOrWhitespace(errors, "personMail");

        rejectIfEmptyOrWhitespace(errors, "dataSourceName");
        rejectIfNullOrEmpty(errors, "dataTypes");

        rejectIfEmptyOrWhitespace(errors, "proxyServiceURL");

        rejectIfEmptyOrWhitespace(errors, "iplugAdminGuiUrl");
        rejectIfEmptyOrWhitespace(errors, "iplugAdminGuiPort");
        try {
            final String property = System.getProperty(IKeys.PORT);
            if (property != null) {
                final Integer jettyPort = Integer.parseInt(property);
                final Integer port = (Integer) errors.getFieldValue("iplugAdminGuiPort");
                if (!port.equals(jettyPort)) {
                    final Socket socket = new Socket(InetAddress.getLocalHost(), port);
                    socket.close();
                    // no errors? then the socket is already taken
                    rejectError(errors, "iplugAdminGuiPort", IErrorKeys.INVALID);
                }
            }
        } catch (final Exception e) {
        }
        rejectIfEmptyOrWhitespace(errors, "iplugAdminPassword");

        return errors;
    }

    public final Errors validatePartners(final Errors errors) {
        rejectIfNullOrEmpty(errors, "partners");
        return errors;
    }

    public final Errors validateProviders(final Errors errors, final boolean hasBusConnection) {
        if (hasBusConnection) {
            rejectIfNullOrEmpty(errors, "providers");
        }
        return errors;
    }
}
