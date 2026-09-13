package com.licensis.notaire.application.port.in.payment;

import com.licensis.notaire.domain.payment.PaymentDetails;

/**
 * Inbound port for editing an already registered payment (CU15).
 *
 * <p>The edit is a partial update: only the non-null fields of the command are applied.
 */
public interface EditPaymentUseCase {

    /**
     * Applies the non-null fields of the command to an existing payment.
     *
     * @param command the changes to apply
     * @return the updated payment
     * @throws IllegalArgumentException if the payment does not exist or the amount is not positive
     */
    PaymentDetails edit(EditPaymentCommand command);
}
