package com.licensis.notaire.dto;

import java.util.Date;

/**
 * CU10 - Read-model for a single "Entidad Externa" document tracked within a
 * gestión, exposing exactly the fields the use case's Curso de Eventos (paso 4)
 * requires: nombre, preparado, número de cartón, fecha de ingreso, fecha de
 * salida, observado, monto deuda, fecha de pago, fecha de liberación,
 * observaciones y si fue finalizado (entregado).
 */
public record DtoDocumentoEntidadExterna(
        Integer idDocumentoPresentado,
        String nombre,
        Boolean preparado,
        Integer numeroCarton,
        Date fechaIngreso,
        Date fechaSalida,
        Boolean observado,
        Float importeAPagar,
        Date fechaPago,
        Date fechaLiberado,
        String observaciones,
        Boolean entregado) {
}
