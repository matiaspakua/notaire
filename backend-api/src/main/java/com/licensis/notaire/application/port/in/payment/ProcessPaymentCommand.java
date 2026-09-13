package com.licensis.notaire.application.port.in.payment;

import java.util.Date;

/**
 * Input of {@link ProcessPaymentUseCase} (CU15).
 *
 * <p>{@link Date} is mutable, so it is defensively copied on construction and access.
 *
 * @param budgetId      budget the payment applies to
 * @param amount        amount to pay; must be positive
 * @param date          date of the payment; {@code null} defaults to now when registered
 * @param notes         free-form observations, may be {@code null}
 * @param paymentMethod free-form payment method, may be {@code null}
 */
public record ProcessPaymentCommand(
        Integer budgetId,
        Float amount,
        Date date,
        String notes,
        String paymentMethod) {

    public ProcessPaymentCommand {
        date = date == null ? null : new Date(date.getTime());
    }

    @Override
    public Date date() {
        return date == null ? null : new Date(date.getTime());
    }
}
