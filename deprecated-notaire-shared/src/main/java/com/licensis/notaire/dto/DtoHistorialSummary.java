package com.licensis.notaire.dto;

import java.util.Date;

/**
 * Read-model for historial endpoints. A POJO record without dependencies
 * on backend entity classes; mapping from `Historial` is performed in the
 * `backend-api` module where the entity type is available.
 */
public record DtoHistorialSummary(
        Integer idHistorial,
        Date fecha,
        String observaciones,
        Integer gestionId,
        Integer estadoGestionId,
        String estadoGestionNombre) {
}
