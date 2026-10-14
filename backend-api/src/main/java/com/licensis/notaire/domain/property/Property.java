package com.licensis.notaire.domain.property;

/**
 * A real estate property in the notary's files. Plain Java: creating and replacing validate, restoring a stored
 * row does not, because legacy rows predate the rules (issue #1702, ADR-030).
 */
public record Property(Integer id, PropertyDetails details, int version) {

    public static Property create(PropertyDetails details) {
        return new Property(null, validated(details), 0);
    }

    public static Property restore(Integer id, PropertyDetails details, int version) {
        return new Property(id, details, version);
    }

    public Property replaceDetails(PropertyDetails newDetails) {
        return new Property(id, validated(newDetails), version);
    }

    public Property withIdentity(Integer newId, int newVersion) {
        return new Property(newId, details, newVersion);
    }

    /** The cadastral designation identifies the property: it is required, and blank counts as missing. */
    private static PropertyDetails validated(PropertyDetails details) {
        if (details.cadastralDesignation() == null || details.cadastralDesignation().isBlank()) {
            throw new IllegalArgumentException("cadastral designation is required");
        }
        return details;
    }
}
