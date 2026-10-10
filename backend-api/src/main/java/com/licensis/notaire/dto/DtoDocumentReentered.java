package com.licensis.notaire.dto;

/**
 * CU43 - The {@code SubmittedDocument} recién creado by a reentry, with
 * the datos heredados of its {@code DocumentType}.
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
