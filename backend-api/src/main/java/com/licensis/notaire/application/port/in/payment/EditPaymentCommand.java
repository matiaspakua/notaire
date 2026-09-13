package com.licensis.notaire.application.port.in.payment;

import java.util.Date;

/**
 * Input of {@link EditPaymentUseCase}. Every field except {@code paymentId} is optional:
 * a {@code null} means "leave unchanged".
 *
 * @param paymentId     payment to edit
 * @param amount        new amount; must be positive when present
 * @param date          new date, or {@code null} to leave unchanged
 * @param notes         new observations, or {@code null} to leave unchanged
 * @param paymentMethod new payment method, or {@code null} to leave unchanged
 */
public record EditPaymentCommand(
        Integer paymentId,
        Float amount,
        Date date,
        String notes,
        String paymentMethod) {

    public EditPaymentCommand {
        date = date == null ? null : new Date(date.getTime());
    }

    @Override
    public Date date() {
        return date == null ? null : new Date(date.getTime());
    }
}
