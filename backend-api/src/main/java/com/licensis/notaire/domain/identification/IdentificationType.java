package com.licensis.notaire.domain.identification;

/**
 * A kind of identification document people can have (DNI, passport, …) and how many characters its number has.
 * Plain Java: creation and renaming validate, restoring a stored row does not, because legacy rows predate
 * the rules (issue #1700, ADR-030).
 */
public record IdentificationType(Integer id, String name, String characters, int version) {

    public static IdentificationType create(String name, String characters) {
        return new IdentificationType(null, requireName(name), characters, 0);
    }

    public static IdentificationType restore(Integer id, String name, String characters, int version) {
        return new IdentificationType(id, name, characters, version);
    }

    public IdentificationType changeTo(String newName, String newCharacters) {
        return new IdentificationType(id, requireName(newName), newCharacters, version);
    }

    /** One row per name, whatever the letter case or surrounding spaces (issue #1361). */
    public boolean hasSameNameAs(String other) {
        return name != null && other != null && name.trim().equalsIgnoreCase(other.trim());
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        return name.trim();
    }
}
