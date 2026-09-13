package com.licensis.notaire.application.port.out.payment;

import com.licensis.notaire.domain.payment.PaymentDetails;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Outbound port describing the payment persistence capabilities the application needs.
 *
 * <p>Deliberately not a pass-through of {@code PaymentRepository}: it exposes only the
 * operations the use cases actually exercise, and speaks {@link PaymentDetails} rather
 * than the JPA {@code Payment} entity, so no managed entity or lazy proxy escapes the
 * persistence adapter.
 */
public interface PaymentRepositoryPort {

    List<PaymentDetails> findAll();

    Optional<PaymentDetails> findById(Integer paymentId);

    List<PaymentDetails> findByBudgetId(Integer budgetId);

    List<PaymentDetails> findByDateRange(Date startDate, Date endDate);

    /**
     * Sum of every amount paid for the budget, or {@code null} when no payment exists.
     */
    Float sumAmountByBudgetId(Integer budgetId);

    boolean existsById(Integer paymentId);

    void deleteById(Integer paymentId);

    /**
     * Persists a brand new payment against an existing budget.
     */
    PaymentDetails register(NewPayment payment);

    /**
     * Applies the non-null fields of {@code changes} to an existing payment.
     */
    PaymentDetails applyChanges(Integer paymentId, PaymentChanges changes);
}
