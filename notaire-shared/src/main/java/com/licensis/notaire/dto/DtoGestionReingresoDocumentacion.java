package com.licensis.notaire.dto;

import java.util.List;

/**
 * CU43 - Read-model para elegir qué reingresar: los trámites de una gestión
 * junto con la documentación necesaria de cada uno.
 */
public record DtoGestionReingresoDocumentacion(
        Integer idGestion,
        int numero,
        String encabezado,
        List<DtoTramiteDocumentacionNecesaria> tramites) {
}
