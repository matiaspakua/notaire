package com.licensis.notaire.application.port.in.payment;

import com.licensis.notaire.domain.payment.PaymentDetails;
import com.licensis.notaire.exception.PendingBalanceExceededException;

/**
 * Inbound port for CU15 "Procesar Pago": register a payment against a budget.
 *
 * <p>Driving adapters (REST, CLI, batch) depend on this contract; it deliberately
 * speaks only plain values and domain types, never HTTP or JPA types.
 */
public interface ProcessPaymentUseCase {

    /**
     * Registers a payment for a budget after validating it against the pending balance.
     *
     * @param command the payment to register
     * @return the persisted payment
     * @throws IllegalArgumentException          if the budget does not exist or the amount is not positive
     * @throws PendingBalanceExceededException   if the amount exceeds the budget's pending balance
     */
    PaymentDetails process(ProcessPaymentCommand command);
}
