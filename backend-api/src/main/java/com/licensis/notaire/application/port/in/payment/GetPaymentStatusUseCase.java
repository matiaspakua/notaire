package com.licensis.notaire.application.port.in.payment;

import com.licensis.notaire.domain.payment.PaymentStatus;

/**
 * Inbound port for CU47 "Consultar Estado de Pago": the financial position of a budget.
 */
public interface GetPaymentStatusUseCase {

    /**
     * Remaining balance of the budget: its total value minus everything paid so far.
     *
     * @throws IllegalArgumentException if the budget does not exist
     */
    float pendingBalance(Integer budgetId);

    /**
     * Aggregated payment status of the budget.
     *
     * @throws IllegalArgumentException if the budget does not exist
     */
    PaymentStatus status(Integer budgetId);
}
