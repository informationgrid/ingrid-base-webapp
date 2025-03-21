/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.admin.validation;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import de.ingrid.admin.command.CommunicationCommandObject;

@Service
public class CommunicationValidator extends AbstractValidator<CommunicationCommandObject> {

    public final Errors validateProxyUrl(final Errors errors, final String defaultUrl) {
        rejectIfEmptyOrWhitespace(errors, "proxyServiceUrl");
        if (defaultUrl.equals(get(errors, "proxyServiceUrl"))) {
            rejectError(errors, "proxyServiceUrl", IErrorKeys.INVALID);
        }
        return errors;
    }

    public final Errors validateBus(final Errors errors) {
        rejectIfEmptyOrWhitespace(errors, "busProxyServiceUrl");
        rejectIfEmptyOrWhitespace(errors, "ip");
        rejectIfEmptyOrWhitespace(errors, "port");

        final Integer port = getInteger(errors, "port");
        if (null != port && port < 0) {
            rejectError(errors, "port", IErrorKeys.INVALID);
        }

        return errors;
    }
}
