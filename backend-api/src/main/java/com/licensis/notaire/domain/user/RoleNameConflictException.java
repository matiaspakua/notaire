package com.licensis.notaire.domain.user;

/** Raised when creating a role whose name already exists (issue #1731). */
public class RoleNameConflictException extends RuntimeException {

    public RoleNameConflictException(String name) {
        super("Already exists a role with the name: " + name);
    }
}
