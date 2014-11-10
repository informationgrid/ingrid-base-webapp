/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
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
            rejectIfEmptyOrWhitespace(errors, "organisationPartnerAbbr");
        }

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
