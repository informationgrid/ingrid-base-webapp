package de.ingrid.admin.security;

import java.security.Principal;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.security.UserRealm;

import de.ingrid.admin.security.IngridPrincipal.KnownPrincipal;

public class IngridRealm implements UserRealm {

    private final Log LOG = LogFactory.getLog(IngridRealm.class);

    public IngridRealm() {
        System.setProperty("java.security.auth.login.config", System.getProperty("user.dir") + "/conf/ingrid.auth");
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
            LOG.error("login error for user: " + userName);
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
