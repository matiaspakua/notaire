package com.licensis.notaire.domain.user;

/** Raised when a role id is absent (issue #1731). */
public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(int id) {
        super("Role not found: " + id);
    }
}
