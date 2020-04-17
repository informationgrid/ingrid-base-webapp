/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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
package de.ingrid.admin.security;

import java.io.IOException;
import java.security.Principal;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.security.UserRealm;
import org.springframework.core.io.ClassPathResource;

import de.ingrid.admin.security.IngridPrincipal.KnownPrincipal;

public class IngridRealm implements UserRealm {

    private final Log LOG = LogFactory.getLog(IngridRealm.class);

    public IngridRealm() throws IOException {
        ClassPathResource authFile = new ClassPathResource( "ingrid.auth" );
        System.setProperty("java.security.auth.login.config", authFile.getFile().getPath());
    }

    @Override
    public Principal authenticate(String userName, Object password, Request request) {

        Principal principal = null;
        try {
            RequestCallbackHandler handler = new RequestCallbackHandler(request);
            String[] url = request.getRequestURL().toString().split("/base/auth/j_security_check");
            // remember redirect url to jump to after initialization
            request.getSession().setAttribute("redirectUrl", request.getSession().getAttribute("org.mortbay.jetty.URI"));
            // automatically redirect to the welcome page, which initialize plug description into session
            request.getSession().setAttribute("org.mortbay.jetty.URI", url[0].concat("/base/welcome.html"));
            LoginContext loginContext = new LoginContext("IngridLogin", handler);
            loginContext.login();
            Subject subject = loginContext.getSubject();
            Set<Principal> principals = subject.getPrincipals();
            Principal tmpPrincipal = principals.isEmpty() ? principal : principals.iterator().next();
            if (tmpPrincipal instanceof KnownPrincipal) {
                KnownPrincipal knownPrincipal = (KnownPrincipal) tmpPrincipal;
                knownPrincipal.setLoginContext(loginContext);
                principal = knownPrincipal;
                LOG.info("principal has logged in: " + principal);
            }
        } catch (LoginException e) {
            LOG.error("login error for user: " + userName, e);
        }
        if (principal == null) {
            LOG.info("login failed for userName: " + userName);
        }
        return principal;
    }

    @Override
    public void disassociate(Principal principal) {
        // nothing todo
    }

    @Override
    public String getName() {
        return IngridRealm.class.getSimpleName();
    }

    @Override
    public Principal getPrincipal(String name) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean isUserInRole(Principal principal, String role) {
        boolean bit = false;
        if (principal instanceof KnownPrincipal) {
            KnownPrincipal knownPrincipal = (KnownPrincipal) principal;
            bit = knownPrincipal.isInRole(role);
        }
        return bit;
    }

    @Override
    public void logout(Principal principal) {
        try {
            if (principal instanceof KnownPrincipal) {
                KnownPrincipal knownPrincipal = (KnownPrincipal) principal;
                LoginContext loginContext = knownPrincipal.getLoginContext();
                if (loginContext != null) {
                    loginContext.logout();
                }
                LOG.info("principal has logged out: " + knownPrincipal);
            }
        } catch (LoginException e) {
            LOG.warn("logout failed", e);
        }
    }

    @Override
    public Principal popRole(Principal principal) {
        return principal;
    }

    @Override
    public Principal pushRole(Principal principal, String role) {
        return principal;
    }

    @Override
    public boolean reauthenticate(Principal principal) {
        return (principal instanceof KnownPrincipal);
    }

}
