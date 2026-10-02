package com.licensis.notaire.domain.payment;

import java.math.BigDecimal;

/**
 * Aggregated payment status of a budget, derived from its pending balance
 * (CU15 / CU47, Issue #821).
 *
 * <p>The constant names are part of the published REST contract: they are serialised
 * verbatim by {@code GET /api/v1/pagos/presupuesto/{id}/estado}. Do not rename them
 * without versioning the API.
 */
public enum PaymentStatus {

    /** No payment has been registered for the budget yet. */
    NoPayments,

    /** Some amount has been paid but a balance is still pending. */
    PARTIAL,

    /** The budget is fully paid. */
    PAID;

    /**
     * Derives the status from the amount already paid and the remaining balance.
     *
     * @param totalPaid      amount already paid; {@code null} means nothing was paid
     * @param pendingBalance remaining balance of the budget
     */
    public static PaymentStatus of(BigDecimal totalPaid, BigDecimal pendingBalance) {
        if (totalPaid == null || totalPaid.compareTo(BigDecimal.ZERO) == 0) {
            return NoPayments;
        }
        return pendingBalance.compareTo(BigDecimal.ZERO) <= 0 ? PAID : PARTIAL;
    }
}
