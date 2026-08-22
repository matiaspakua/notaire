package com.licensis.notaire.dto;

import java.util.Date;

/**
 * API response shape for a payment (Pago), carrying the identifier of its
 * associated presupuesto instead of the JPA entity relation.
 */
public record DtoPagoResponse(
        Integer idPago,
        Integer idPresupuesto,
        Float monto,
        Date fecha,
        String metodoPago,
        String observaciones) {
}
