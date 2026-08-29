package com.licensis.notaire.dto;

import java.util.List;

/**
 * CU43 - Un trámite de la gestión junto con la documentación necesaria según
 * su {@code PlantillaTramite}, usada para elegir qué tipo de documento
 * reingresar.
 */
public record DtoTramiteDocumentacionNecesaria(
        Integer idTramite,
        String tipoTramiteNombre,
        List<DtoDocumentoNecesario> documentosNecesarios) {
}
