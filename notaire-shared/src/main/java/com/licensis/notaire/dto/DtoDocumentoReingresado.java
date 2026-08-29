package com.licensis.notaire.dto;

/**
 * CU43 - El {@code DocumentoPresentado} recién creado por un reingreso, con
 * los datos heredados de su {@code TipoDeDocumento}.
 */
public record DtoDocumentoReingresado(
        Integer idDocumentoPresentado,
        Integer idTramite,
        Integer idTipoDocumento,
        String nombre,
        boolean vence,
        Integer diasVencimiento,
        String quienEntrega,
        boolean reingresado) {
}
