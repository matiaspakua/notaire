package com.licensis.notaire.dto;

/**
 * CU43 - The {@code SubmittedDocument} just created by a re-entry, with
 * the data copied from its {@code DocumentType}.
 */
public record DtoDocumentReentered(
        Integer idSubmittedDocument,
        Integer idProcedure,
        Integer idDocumentType,
        String name,
        boolean expires,
        Integer dueDays,
        String deliveredBy,
        boolean reentered) {
}
