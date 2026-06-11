package com.licensis.notaire.dto;

import com.licensis.notaire.negocio.Historial;

import java.util.Date;

/**
 * Read-model for historial endpoints. Avoids serializing the JPA entity,
 * whose eager gestión back-reference produces a cycle
 * (gestión → historialList → historial → gestión).
 */
public record DtoHistorialSummary(
        Integer idHistorial,
        Date fecha,
        String observaciones,
        Integer gestionId,
        Integer estadoGestionId,
        String estadoGestionNombre) {

    public static DtoHistorialSummary from(Historial historial) {
        return new DtoHistorialSummary(
                historial.getIdHistorial(),
                historial.getFecha(),
                historial.getObservaciones(),
                historial.getFkIdGestion() != null ? historial.getFkIdGestion().getIdGestion() : null,
                historial.getFkIdEstadoGestion() != null
                        ? historial.getFkIdEstadoGestion().getIdEstadoGestion() : null,
                historial.getFkIdEstadoGestion() != null
                        ? historial.getFkIdEstadoGestion().getNombre() : null);
    }
}
