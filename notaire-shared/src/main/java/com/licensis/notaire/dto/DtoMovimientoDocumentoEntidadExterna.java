package com.licensis.notaire.dto;

import java.util.Date;

/**
 * CU10 - Request body for registering the movement data of a single
 * "Entidad Externa" document (paso 5 del Curso de Eventos).
 */
public record DtoMovimientoDocumentoEntidadExterna(
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
