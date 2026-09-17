package com.licensis.notaire.application.port.out.payment;

import java.util.Date;

/**
 * Write model handed to {@link PaymentRepositoryPort#register(NewPayment)}.
 *
 * @param budgetId      budget the payment belongs to
 * @param amount        validated, positive amount
 * @param date          date of the payment, never {@code null} at this point
 * @param notes         free-form observations, may be {@code null}
 * @param paymentMethod free-form payment method, may be {@code null}
 */
public record NewPayment(
        Integer budgetId,
        float amount,
        Date date,
        String notes,
        String paymentMethod) {

    public NewPayment {
        date = date == null ? null : new Date(date.getTime());
    }

    @Override
    public Date date() {
        return date == null ? null : new Date(date.getTime());
    }
}
