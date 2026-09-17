package com.licensis.notaire.application.port.out.payment;

import java.util.Date;

/**
 * Partial write model handed to
 * {@link PaymentRepositoryPort#applyChanges(Integer, PaymentChanges)}: a {@code null}
 * field means "leave the stored value untouched".
 *
 * @param amount        new amount, already validated when present
 * @param date          new date, or {@code null} to leave unchanged
 * @param notes         new observations, or {@code null} to leave unchanged
 * @param paymentMethod new payment method, or {@code null} to leave unchanged
 */
public record PaymentChanges(Float amount, Date date, String notes, String paymentMethod) {

    public PaymentChanges {
        date = date == null ? null : new Date(date.getTime());
    }

    @Override
    public Date date() {
        return date == null ? null : new Date(date.getTime());
    }
}
