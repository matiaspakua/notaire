package com.licensis.notaire.domain.user;

/** Base type for login failures mapped by {@code UserLoginExceptionHandler}. */
public class LoginException extends RuntimeException {

    protected LoginException(String message) {
        super(message);
    }
}
