/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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

import java.security.Principal;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractLoginModule implements LoginModule {

    private final Log LOG = LogFactory.getLog(AbstractLoginModule.class);

    private Subject _subject;
    private CallbackHandler _callbackHandler;
    private boolean _authenticated;

    private IngridPrincipal _currentPrincipal;

    private boolean _committed;

    @Override
    public boolean abort() throws LoginException {
        _currentPrincipal = null;
        return (isAuthenticated() && isCommitted());
    }

    @Override
    public boolean commit() throws LoginException {
        if (!isAuthenticated()) {
            _currentPrincipal = null;
            setCommitted(false);
        } else {
            Set<Principal> principals = _subject.getPrincipals();
            principals.add(_currentPrincipal);
            setCommitted(true);
        }
        return isCommitted();
    }

    private void setCommitted(boolean committed) {
        _committed = committed;
    }

    private boolean isCommitted() {
        return _committed;
    }

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        _subject = subject;
        _callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        NameCallback nameCallback = new NameCallback("user name:");
        PasswordCallback passwordCallback = new PasswordCallback("password:", false);
        try {
            _callbackHandler.handle(new Callback[] { nameCallback, passwordCallback });
            String name = nameCallback.getName();
            char[] password = passwordCallback.getPassword();
            if (name != null) {
                if (password != null) {
                    IngridPrincipal ingridPrincipal = authenticate(name, new String(password));
                    if (ingridPrincipal.isAuthenticated()) {
                        setAuthenticated(true);
                        _currentPrincipal = ingridPrincipal;
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("login failed.", e);
            throw new LoginException(e.getMessage());
        }
        return isAuthenticated();
    }

    private boolean isAuthenticated() {
        return _authenticated;
    }

    private void setAuthenticated(boolean authenticated) {
        _authenticated = authenticated;
    }

    @Override
    public boolean logout() throws LoginException {
        Set<Principal> principals = _subject.getPrincipals();
        principals.remove(_currentPrincipal);
        return true;
    }

    protected abstract IngridPrincipal authenticate(String userName, String password);

}
