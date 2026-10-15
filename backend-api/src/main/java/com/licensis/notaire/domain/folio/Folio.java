package com.licensis.notaire.domain.folio;

/**
 * A folio instance (protocol entry). Plain Java (issue #1727, ADR-030). Status {@code Utilizado} means the folio is
 * linked to a deed and locked against unrelated edits/deletes.
 */
public record Folio(
        Integer id,
        int number,
        int year,
        String status,
        String notes,
        Integer folioTypeId,
        String folioTypeName,
        String folioTypeNotes,
        boolean folioTypeEnabled,
        boolean folioTypeAuxiliary,
        Integer notaryId,
        String notaryFirstName,
        String notaryLastName,
        String notaryIdentificationNumber,
        Integer notaryRegistrationNumber,
        Integer deedId,
        Integer deedNumber,
        String deedStatus,
        int version) {

    public static final String STATUS_USED = "Utilizado";

    public static Folio create(
            int number,
            int year,
            String status,
            String notes,
            Integer folioTypeId,
            Integer notaryId,
            Integer deedId) {
        String effectiveStatus = deedId != null ? STATUS_USED : status;
        return new Folio(
                null,
                number,
                year,
                effectiveStatus,
                notes,
                folioTypeId,
                null,
                null,
                false,
                false,
                notaryId,
                null,
                null,
                null,
                null,
                deedId,
                null,
                null,
                0);
    }

    public static Folio restore(
            Integer id,
            int number,
            int year,
            String status,
            String notes,
            Integer folioTypeId,
            String folioTypeName,
            String folioTypeNotes,
            boolean folioTypeEnabled,
            boolean folioTypeAuxiliary,
            Integer notaryId,
            String notaryFirstName,
            String notaryLastName,
            String notaryIdentificationNumber,
            Integer notaryRegistrationNumber,
            Integer deedId,
            Integer deedNumber,
            String deedStatus,
            int version) {
        return new Folio(
                id,
                number,
                year,
                status,
                notes,
                folioTypeId,
                folioTypeName,
                folioTypeNotes,
                folioTypeEnabled,
                folioTypeAuxiliary,
                notaryId,
                notaryFirstName,
                notaryLastName,
                notaryIdentificationNumber,
                notaryRegistrationNumber,
                deedId,
                deedNumber,
                deedStatus,
                version);
    }

    public boolean isInUse() {
        return STATUS_USED.equals(status);
    }

    public boolean linksSameDeed(Integer requestDeedId) {
        return deedId != null && requestDeedId != null && deedId.equals(requestDeedId);
    }

    public Folio withIdentity(Integer newId, int newVersion) {
        return new Folio(
                newId,
                number,
                year,
                status,
                notes,
                folioTypeId,
                folioTypeName,
                folioTypeNotes,
                folioTypeEnabled,
                folioTypeAuxiliary,
                notaryId,
                notaryFirstName,
                notaryLastName,
                notaryIdentificationNumber,
                notaryRegistrationNumber,
                deedId,
                deedNumber,
                deedStatus,
                newVersion);
    }

    public Folio changeTo(
            int newNumber,
            int newYear,
            String newStatus,
            String newNotes,
            Integer newTypeId,
            Integer newNotaryId,
            Integer newDeedId) {
        String effectiveStatus = newDeedId != null ? STATUS_USED : newStatus;
        return new Folio(
                id,
                newNumber,
                newYear,
                effectiveStatus,
                newNotes,
                newTypeId != null ? newTypeId : folioTypeId,
                folioTypeName,
                folioTypeNotes,
                folioTypeEnabled,
                folioTypeAuxiliary,
                newNotaryId != null ? newNotaryId : notaryId,
                notaryFirstName,
                notaryLastName,
                notaryIdentificationNumber,
                notaryRegistrationNumber,
                newDeedId != null ? newDeedId : deedId,
                deedNumber,
                deedStatus,
                version);
    }
}
