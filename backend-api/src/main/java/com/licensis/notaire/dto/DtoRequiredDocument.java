package com.licensis.notaire.dto;

/**
 * CU43 - A document type requerido by the {@code ProcedureTemplate} of a
 * procedure, with the datos that the Gestor/Notary necesita for decidir if
 * reingresarlo: name, if vence, days of expiration and who it entrega.
 */
public record DtoRequiredDocument(
        Integer idDocumentType,
        String name,
        boolean expires,
        Integer dueDays,
        String deliveredBy) {
}
