package com.licensis.notaire.domain.folio;

/**
 * A kind of folio (protocol, auxiliary protocol, …). Plain Java: creating and changing validate, restoring a
 * stored row does not, because legacy rows predate the rules (issue #1703, ADR-030). The only rule is the one the
 * database column enforced before: a name is required.
 */
public record FolioType(Integer id, String name, String notes, boolean enabled, boolean auxiliary, int version) {

    public static FolioType create(String name, String notes, boolean enabled, boolean auxiliary) {
        return new FolioType(null, requireName(name), notes, enabled, auxiliary, 0);
    }

    public static FolioType restore(Integer id, String name, String notes, boolean enabled, boolean auxiliary,
            int version) {
        return new FolioType(id, name, notes, enabled, auxiliary, version);
    }

    public FolioType changeTo(String newName, String newNotes, boolean newEnabled, boolean newAuxiliary) {
        return new FolioType(id, requireName(newName), newNotes, newEnabled, newAuxiliary, version);
    }

    public FolioType withIdentity(Integer newId, int newVersion) {
        return new FolioType(newId, name, notes, enabled, auxiliary, newVersion);
    }

    private static String requireName(String name) {
        if (name == null) {
            throw new FolioTypeInvalidException("name is required");
        }
        return name;
    }
}
