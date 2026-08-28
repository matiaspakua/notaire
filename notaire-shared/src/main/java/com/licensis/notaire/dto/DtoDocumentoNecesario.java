package com.licensis.notaire.dto;

/**
 * CU43 - Un tipo de documento requerido por la {@code PlantillaTramite} de un
 * trámite, con los datos que el Gestor/Escribano necesita para decidir si
 * reingresarlo: nombre, si vence, días de vencimiento y quién lo entrega.
 */
public record DtoDocumentoNecesario(
        Integer idTipoDocumento,
        String nombre,
        boolean vence,
        Integer diasVencimiento,
        String quienEntrega) {
}
