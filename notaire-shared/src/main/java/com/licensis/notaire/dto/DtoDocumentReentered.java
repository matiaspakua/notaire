package com.licensis.notaire.dto;

/**
 * CU43 - El {@code DocumentoPresentado} recién creado por un reingreso, con
 * los datos heredados de su {@code TipoDeDocumento}.
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
