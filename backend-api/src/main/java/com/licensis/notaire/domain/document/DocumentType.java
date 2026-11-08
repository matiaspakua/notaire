package com.licensis.notaire.domain.document;

/**
 * A kind of submitted document (ID card, deed copy, …). Plain Java: creating and changing validate, restoring a
 * stored row does not, because legacy rows predate the rules (issue #1705, ADR-030). The only creation rule is the
 * one the database column enforced before: a name is required (not null).
 */
public record DocumentType(
        Integer id,
        String name,
        boolean expires,
        Integer dueDays,
        String deliveredBy,
        boolean enabled,
        boolean returned,
        int version) {

    public static DocumentType create(
            String name, boolean expires, Integer dueDays, String deliveredBy, boolean enabled, boolean returned) {
        return new DocumentType(
                null, requireName(name), expires, dueDaysWhenExpires(expires, dueDays), deliveredBy, enabled, returned,
                0);
    }

    public static DocumentType restore(
            Integer id,
            String name,
            boolean expires,
            Integer dueDays,
            String deliveredBy,
            boolean enabled,
            boolean returned,
            int version) {
        return new DocumentType(id, name, expires, dueDays, deliveredBy, enabled, returned, version);
    }

    public DocumentType changeTo(
            String newName,
            boolean newExpires,
            Integer newDueDays,
            String newDeliveredBy,
            boolean newEnabled,
            boolean newReturned) {
        return new DocumentType(
                id,
                requireName(newName),
                newExpires,
                dueDaysWhenExpires(newExpires, newDueDays),
                newDeliveredBy,
                newEnabled,
                newReturned,
                version);
    }

    public DocumentType withIdentity(Integer newId, int newVersion) {
        return new DocumentType(newId, name, expires, dueDays, deliveredBy, enabled, returned, newVersion);
    }

    private static String requireName(String name) {
        if (name == null) {
            throw new DocumentTypeInvalidException("name is required");
        }
        return name;
    }

    private static Integer dueDaysWhenExpires(boolean expires, Integer dueDays) {
        return expires ? dueDays : null;
    }
}
