package com.licensis.notaire.dto;

import java.util.Date;

/**
 * API response shape for a payment (Pago), carrying the identifier of its
 * associated presupuesto instead of the JPA entity relation.
 */
public record DtoPaymentResponse(
        Integer idPayment,
        Integer idBudget,
        Float amount,
        Date date,
        String paymentMethod,
        String notes) {
}
