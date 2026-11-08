package com.licensis.notaire.domain.user;

/** Raised when a user id is absent (issue #1731). */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(int id) {
        super("User not found: " + id);
    }
}
