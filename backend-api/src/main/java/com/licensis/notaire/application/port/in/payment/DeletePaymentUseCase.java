package com.licensis.notaire.application.port.in.payment;

/**
 * Inbound port for removing a registered payment (CU15).
 */
public interface DeletePaymentUseCase {

    /**
     * Deletes the payment.
     *
     * @throws IllegalArgumentException if the payment does not exist
     */
    void delete(Integer paymentId);
}
