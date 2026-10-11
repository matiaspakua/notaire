package com.licensis.notaire.dto;

/**
 * CU43 - A document type required by the {@code ProcedureTemplate} of a
 * procedure, with the data the clerk or notary needs to decide whether to
 * re-enter it: name, whether it expires, due days, and who delivers it.
 */
public record DtoRequiredDocument(
        Integer idDocumentType,
        String name,
        boolean expires,
        Integer dueDays,
        String deliveredBy) {
}
