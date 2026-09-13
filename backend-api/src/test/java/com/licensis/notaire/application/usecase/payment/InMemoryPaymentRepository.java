package com.licensis.notaire.application.usecase.payment;

import com.licensis.notaire.application.port.out.payment.NewPayment;
import com.licensis.notaire.application.port.out.payment.PaymentChanges;
import com.licensis.notaire.application.port.out.payment.PaymentRepositoryPort;
import com.licensis.notaire.domain.payment.PaymentDetails;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory fake of {@link PaymentRepositoryPort} used to unit test the payment use
 * cases without Spring, JPA or Mockito.
 *
 * <p>Having a second implementation of the port alongside the JPA one is deliberate:
 * it validates that the port design is implementation-agnostic.
 */
class InMemoryPaymentRepository implements PaymentRepositoryPort {

    private final Map<Integer, PaymentDetails> stored = new LinkedHashMap<>();
    private int nextId = 1;

    /** Seeds a payment without going through the use cases. */
    PaymentDetails given(PaymentDetails payment) {
        Integer id = payment.id() != null ? payment.id() : nextId++;
        PaymentDetails withId = new PaymentDetails(
                id, payment.budgetId(), payment.amount(), payment.date(),
                payment.paymentMethod(), payment.notes());
        stored.put(id, withId);
        return withId;
    }

    @Override
    public List<PaymentDetails> findAll() {
        return List.copyOf(stored.values());
    }

    @Override
    public Optional<PaymentDetails> findById(Integer paymentId) {
        return Optional.ofNullable(stored.get(paymentId));
    }

    @Override
    public List<PaymentDetails> findByBudgetId(Integer budgetId) {
        return stored.values().stream()
                .filter(payment -> java.util.Objects.equals(payment.budgetId(), budgetId))
                .toList();
    }

    @Override
    public List<PaymentDetails> findByDateRange(Date startDate, Date endDate) {
        return stored.values().stream()
                .filter(payment -> payment.date() != null)
                .filter(payment -> !payment.date().before(startDate) && !payment.date().after(endDate))
                .toList();
    }

    @Override
    public Float sumAmountByBudgetId(Integer budgetId) {
        List<PaymentDetails> forBudget = findByBudgetId(budgetId);
        if (forBudget.isEmpty()) {
            return null;
        }
        float sum = 0f;
        for (PaymentDetails payment : forBudget) {
            sum += payment.amount();
        }
        return sum;
    }

    @Override
    public boolean existsById(Integer paymentId) {
        return stored.containsKey(paymentId);
    }

    @Override
    public void deleteById(Integer paymentId) {
        stored.remove(paymentId);
    }

    @Override
    public PaymentDetails register(NewPayment payment) {
        int id = nextId++;
        PaymentDetails created = new PaymentDetails(
                id, payment.budgetId(), payment.amount(), payment.date(),
                payment.paymentMethod(), payment.notes());
        stored.put(id, created);
        return created;
    }

    @Override
    public PaymentDetails applyChanges(Integer paymentId, PaymentChanges changes) {
        PaymentDetails current = stored.get(paymentId);
        if (current == null) {
            throw new IllegalArgumentException("Pago no encontrado con ID: " + paymentId);
        }
        PaymentDetails updated = new PaymentDetails(
                current.id(),
                current.budgetId(),
                changes.amount() != null ? changes.amount() : current.amount(),
                changes.date() != null ? changes.date() : current.date(),
                changes.paymentMethod() != null ? changes.paymentMethod() : current.paymentMethod(),
                changes.notes() != null ? changes.notes() : current.notes());
        stored.put(paymentId, updated);
        return updated;
    }

    List<PaymentDetails> all() {
        return new ArrayList<>(stored.values());
    }
}
