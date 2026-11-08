package com.licensis.notaire.domain.user;

/** Account is temporarily locked. Clients receive 429 with {@code valid} and {@code message}. */
public class LoginLockedException extends LoginException {

    public LoginLockedException(String message) {
        super(message);
    }
}
