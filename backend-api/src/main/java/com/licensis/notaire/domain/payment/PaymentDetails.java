package com.licensis.notaire.domain.payment;

import java.util.Date;

/**
 * A payment applied to a budget, as the application layer sees it (CU15 / CU47).
 *
 * <p>Framework-free replacement for passing the JPA {@code Payment} entity across
 * layer boundaries: it carries the owning budget's identifier instead of the entity
 * relation, so no lazy proxy can escape a transaction.
 *
 * <p>{@link Date} is mutable, so it is defensively copied on both construction and
 * access to keep this value object immutable.
 *
 * @param id            payment identifier, {@code null} before it is persisted
 * @param budgetId      identifier of the budget the payment applies to
 * @param amount        amount paid
 * @param date          date the payment was applied
 * @param paymentMethod free-form payment method, may be {@code null}
 * @param notes         free-form observations, may be {@code null}
 */
public record PaymentDetails(
        Integer id,
        Integer budgetId,
        float amount,
        Date date,
        String paymentMethod,
        String notes) {

    public PaymentDetails {
        date = copy(date);
    }

    @Override
    public Date date() {
        return copy(date);
    }

    private static Date copy(Date value) {
        return value == null ? null : new Date(value.getTime());
    }
}
