package com.licensis.notaire.domain.user;

/**
 * Application user account. Plain Java (issue #1731, ADR-030). Password field holds the stored hash
 * only — never a plaintext password after encode.
 */
public record User(
        Integer id,
        String name,
        String passwordHash,
        boolean active,
        String type,
        Integer personId,
        String personFirstName,
        String personLastName,
        Integer roleId,
        String roleName,
        int version) {

    public static User create(String name, String passwordHash, boolean active, String type) {
        return new User(null, name, passwordHash, active, type, null, null, null, null, null, 0);
    }

    public static User restore(
            Integer id,
            String name,
            String passwordHash,
            boolean active,
            String type,
            Integer personId,
            String personFirstName,
            String personLastName,
            Integer roleId,
            String roleName,
            int version) {
        return new User(
                id,
                name,
                passwordHash,
                active,
                type,
                personId,
                personFirstName,
                personLastName,
                roleId,
                roleName,
                version);
    }

    public User withCredentials(String newName, String newPasswordHash, boolean newActive, String newType) {
        return new User(
                id,
                newName,
                newPasswordHash != null ? newPasswordHash : passwordHash,
                newActive,
                newType,
                personId,
                personFirstName,
                personLastName,
                roleId,
                roleName,
                version);
    }

    public User withPasswordHash(String newPasswordHash) {
        return new User(
                id,
                name,
                newPasswordHash,
                active,
                type,
                personId,
                personFirstName,
                personLastName,
                roleId,
                roleName,
                version);
    }

    public User withRole(Integer newRoleId, String newRoleName) {
        return new User(
                id,
                name,
                passwordHash,
                active,
                type,
                personId,
                personFirstName,
                personLastName,
                newRoleId,
                newRoleName,
                version);
    }

    public User withIdentity(Integer newId, int newVersion) {
        return new User(
                newId,
                name,
                passwordHash,
                active,
                type,
                personId,
                personFirstName,
                personLastName,
                roleId,
                roleName,
                newVersion);
    }
}
