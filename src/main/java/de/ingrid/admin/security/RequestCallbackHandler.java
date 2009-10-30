package de.ingrid.admin.security;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.mortbay.jetty.Request;

public class RequestCallbackHandler implements CallbackHandler {

    private final Request _request;

    public RequestCallbackHandler(Request request) {
        _request = request;
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        String userName = _request.getParameter("j_username");
        String password = _request.getParameter("j_password");
        ((NameCallback) callbacks[0]).setName(userName);
        ((PasswordCallback) callbacks[1]).setPassword(password.toCharArray());
    }

}
