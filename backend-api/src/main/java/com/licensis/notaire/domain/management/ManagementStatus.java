package com.licensis.notaire.domain.management;

/**
 * A management workflow status (started, archived, …). Plain Java: creating and changing validate; restoring a
 * stored row does not (issue #1719, ADR-030). The name rule matches the database column: required (not null).
 */
public record ManagementStatus(Integer id, String name, String notes, int version) {

    public static ManagementStatus create(String name, String notes) {
        return new ManagementStatus(null, requireName(name), notes, 0);
    }

    public static ManagementStatus restore(Integer id, String name, String notes, int version) {
        return new ManagementStatus(id, name, notes, version);
    }

    public ManagementStatus changeTo(String newName, String newNotes) {
        return new ManagementStatus(id, requireName(newName), newNotes, version);
    }

    public ManagementStatus withIdentity(Integer newId, int newVersion) {
        return new ManagementStatus(newId, name, notes, newVersion);
    }

    private static String requireName(String name) {
        if (name == null) {
            throw new ManagementStatusInvalidException("name is required");
        }
        return name;
    }
}
