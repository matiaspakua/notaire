package com.licensis.notaire.dto;

import java.util.Date;
import java.util.List;

/**
 * CU10 - Read-model for the gestión detail screen (paso 4 del Curso de
 * Eventos): número de gestión, encabezado, fecha de inicio, escribano a
 * cargo, nomenclatura catastral (si la gestión involucra un Inmueble) y la
 * documentación que debe ser presentada por entidades externas.
 */
public record DtoGestionDocumentosEntidadesExternas(
        Integer idGestion,
        int numero,
        String encabezado,
        Date fechaInicio,
        String escribano,
        String nomenclaturaCatastral,
        List<DtoDocumentoEntidadExterna> documentos) {
}
