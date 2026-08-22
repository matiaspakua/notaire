package com.licensis.notaire.dto;

import java.util.List;

/**
 * CU47 financial summary for a presupuesto: its gestión, total, pending
 * balance, and the list of payments applied to it.
 */
public record DtoPresupuestoResumen(
        Integer idPresupuesto,
        int numeroPresupuesto,
        Integer idGestion,
        Integer numeroGestion,
        String encabezadoGestion,
        Float total,
        Float saldoPendiente,
        List<DtoPagoResponse> pagos) {
}
