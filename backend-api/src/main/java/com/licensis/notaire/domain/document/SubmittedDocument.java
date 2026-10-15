package com.licensis.notaire.domain.document;

import java.time.LocalDate;

/**
 * A document presented for a management procedure. Plain Java (issue #1726, ADR-030). Due fields are derived from
 * the document type when the entry is created or when type/date/deliveredBy change.
 */
public record SubmittedDocument(
        Integer id,
        Integer documentTypeId,
        String documentTypeName,
        LocalDate dateEntry,
        Boolean delivered,
        Integer procedureId,
        String name,
        String deliveredBy,
        boolean expires,
        Integer dueDays,
        LocalDate dateDue,
        boolean prepared,
        boolean released,
        boolean flagged,
        boolean reentered) {

    public static SubmittedDocument createNew() {
        return new SubmittedDocument(
                null, null, null, null, false, null, "", "", false, null, null, false, false, false, false);
    }

    public static SubmittedDocument restore(
            Integer id,
            Integer documentTypeId,
            String documentTypeName,
            LocalDate dateEntry,
            Boolean delivered,
            Integer procedureId,
            String name,
            String deliveredBy,
            boolean expires,
            Integer dueDays,
            LocalDate dateDue,
            boolean prepared,
            boolean released,
            boolean flagged,
            boolean reentered) {
        return new SubmittedDocument(
                id,
                documentTypeId,
                documentTypeName,
                dateEntry,
                delivered,
                procedureId,
                name == null ? "" : name,
                deliveredBy == null ? "" : deliveredBy,
                expires,
                dueDays,
                dateDue,
                prepared,
                released,
                flagged,
                reentered);
    }

    public SubmittedDocument withIdentity(Integer newId) {
        return new SubmittedDocument(
                newId,
                documentTypeId,
                documentTypeName,
                dateEntry,
                delivered,
                procedureId,
                name,
                deliveredBy,
                expires,
                dueDays,
                dateDue,
                prepared,
                released,
                flagged,
                reentered);
    }

    public SubmittedDocument withDocumentType(Integer typeId, String typeName) {
        return new SubmittedDocument(
                id,
                typeId,
                typeName,
                dateEntry,
                delivered,
                procedureId,
                name,
                deliveredBy,
                expires,
                dueDays,
                dateDue,
                prepared,
                released,
                flagged,
                reentered);
    }

    public SubmittedDocument withProcedureId(Integer newProcedureId) {
        return new SubmittedDocument(
                id,
                documentTypeId,
                documentTypeName,
                dateEntry,
                delivered,
                newProcedureId,
                name,
                deliveredBy,
                expires,
                dueDays,
                dateDue,
                prepared,
                released,
                flagged,
                reentered);
    }

    public SubmittedDocument withDateEntry(LocalDate newDate) {
        return new SubmittedDocument(
                id,
                documentTypeId,
                documentTypeName,
                newDate,
                delivered,
                procedureId,
                name,
                deliveredBy,
                expires,
                dueDays,
                dateDue,
                prepared,
                released,
                flagged,
                reentered);
    }

    public SubmittedDocument withDelivered(Boolean newDelivered) {
        return new SubmittedDocument(
                id,
                documentTypeId,
                documentTypeName,
                dateEntry,
                newDelivered,
                procedureId,
                name,
                deliveredBy,
                expires,
                dueDays,
                dateDue,
                prepared,
                released,
                flagged,
                reentered);
    }

    public SubmittedDocument withName(String newName) {
        return new SubmittedDocument(
                id,
                documentTypeId,
                documentTypeName,
                dateEntry,
                delivered,
                procedureId,
                newName == null ? "" : newName,
                deliveredBy,
                expires,
                dueDays,
                dateDue,
                prepared,
                released,
                flagged,
                reentered);
    }

    public SubmittedDocument withDue(boolean newExpires, Integer newDueDays, String newDeliveredBy, LocalDate newDue) {
        return new SubmittedDocument(
                id,
                documentTypeId,
                documentTypeName,
                dateEntry,
                delivered,
                procedureId,
                name,
                newDeliveredBy == null ? "" : newDeliveredBy,
                newExpires,
                newDueDays,
                newDue,
                prepared,
                released,
                flagged,
                reentered);
    }

    /**
     * Applies due inheritance from a document type, matching the former controller rules: explicit deliveredBy on
     * the request wins; otherwise the type's deliveredBy is used; dateDue is entry + dueDays when the type expires.
     */
    public SubmittedDocument applyDueFromType(DocumentType type, String explicitDeliveredBy) {
        boolean typeExpires = type != null && type.expires();
        Integer typeDueDays = type == null ? null : type.dueDays();
        String by = explicitDeliveredBy != null
                ? explicitDeliveredBy
                : (type == null || type.deliveredBy() == null ? "" : type.deliveredBy());
        LocalDate due = null;
        if (typeExpires && typeDueDays != null && dateEntry != null) {
            due = dateEntry.plusDays(typeDueDays);
        }
        return withDue(typeExpires, typeDueDays, by, due);
    }
}
