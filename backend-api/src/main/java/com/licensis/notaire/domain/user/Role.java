package com.licensis.notaire.domain.user;

import java.util.List;

/**
 * Application role with module grant list enforced server-side (#1683). Plain Java (issue #1731).
 */
public record Role(
        Integer id, String name, String description, boolean active, List<String> modules, int version) {

    public static Role create(String name, String description, boolean active, List<String> modules) {
        return new Role(null, name, description, active, modules == null ? List.of() : List.copyOf(modules), 0);
    }

    public static Role restore(
            Integer id, String name, String description, boolean active, List<String> modules, int version) {
        return new Role(id, name, description, active, modules == null ? List.of() : List.copyOf(modules), version);
    }

    public Role changeTo(String newName, String newDescription, boolean newActive, List<String> newModules) {
        return new Role(
                id,
                newName,
                newDescription,
                newActive,
                newModules == null ? List.of() : List.copyOf(newModules),
                version);
    }

    public Role withIdentity(Integer newId, int newVersion) {
        return new Role(newId, name, description, active, modules, newVersion);
    }
}
