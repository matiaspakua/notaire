package com.licensis.notaire.adapter.out.persistence.payment;

import com.licensis.notaire.application.port.out.payment.NewPayment;
import com.licensis.notaire.application.port.out.payment.PaymentChanges;
import com.licensis.notaire.application.port.out.payment.PaymentRepositoryPort;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Payment;
import com.licensis.notaire.domain.payment.PaymentDetails;
import com.licensis.notaire.repository.BudgetRepository;
import com.licensis.notaire.repository.PaymentRepository;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Outbound adapter implementing {@link PaymentRepositoryPort} on top of the existing
 * Spring Data JPA repositories.
 *
 * <p>This is the only place where the JPA {@code Payment} entity is touched for the
 * payment slice: every value crossing back into the application layer is a detached
 * {@link PaymentDetails}, so no managed entity or lazy proxy escapes the transaction.
 */
@Component
public class PaymentPersistenceAdapter implements PaymentRepositoryPort {

    private final PaymentRepository paymentRepository;
    private final BudgetRepository budgetRepository;

    public PaymentPersistenceAdapter(PaymentRepository paymentRepository, BudgetRepository budgetRepository) {
        this.paymentRepository = paymentRepository;
        this.budgetRepository = budgetRepository;
    }

    @Override
    public List<PaymentDetails> findAll() {
        return toDetails(paymentRepository.findAll());
    }

    @Override
    public Optional<PaymentDetails> findById(Integer paymentId) {
        return paymentRepository.findById(paymentId).map(PaymentPersistenceAdapter::toDetails);
    }

    @Override
    public List<PaymentDetails> findByBudgetId(Integer budgetId) {
        return toDetails(paymentRepository.findByFkIdBudgetIdBudget(budgetId));
    }

    @Override
    public List<PaymentDetails> findByDateRange(Date startDate, Date endDate) {
        return toDetails(paymentRepository.findByDateBetween(startDate, endDate));
    }

    @Override
    public Float sumAmountByBudgetId(Integer budgetId) {
        return paymentRepository.sumAmountByBudgetId(budgetId);
    }

    @Override
    public boolean existsById(Integer paymentId) {
        return paymentRepository.existsById(paymentId);
    }

    @Override
    public void deleteById(Integer paymentId) {
        paymentRepository.deleteById(paymentId);
    }

    @Override
    public PaymentDetails register(NewPayment newPayment) {
        Budget budget = budgetRepository.findById(newPayment.budgetId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Presupuesto no encontrado con ID: " + newPayment.budgetId()));

        Payment payment = new Payment();
        payment.setAmount(newPayment.amount());
        payment.setDate(newPayment.date());
        payment.setNotes(newPayment.notes());
        payment.setPaymentMethod(newPayment.paymentMethod());
        payment.setBudget(budget);

        return toDetails(paymentRepository.save(payment));
    }

    @Override
    public PaymentDetails applyChanges(Integer paymentId, PaymentChanges changes) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado con ID: " + paymentId));

        if (changes.amount() != null) {
            payment.setAmount(changes.amount());
        }
        if (changes.date() != null) {
            payment.setDate(changes.date());
        }
        if (changes.notes() != null) {
            payment.setNotes(changes.notes());
        }
        if (changes.paymentMethod() != null) {
            payment.setPaymentMethod(changes.paymentMethod());
        }

        return toDetails(paymentRepository.save(payment));
    }

    private static List<PaymentDetails> toDetails(List<Payment> payments) {
        return payments.stream().map(PaymentPersistenceAdapter::toDetails).toList();
    }

    private static PaymentDetails toDetails(Payment payment) {
        return new PaymentDetails(
                payment.getIdPayment(),
                payment.getBudget() != null ? payment.getBudget().getIdBudget() : null,
                payment.getAmount(),
                payment.getDate(),
                payment.getPaymentMethod(),
                payment.getNotes());
    }
}
