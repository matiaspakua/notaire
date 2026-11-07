package com.licensis.notaire.domain.user;

/** Credentials were rejected. Clients receive 200 {@code {"valid": false}}. */
public class LoginRejectedException extends LoginException {

    public LoginRejectedException() {
        super("Invalid login");
    }
}
