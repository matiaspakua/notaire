package com.licensis.notaire.application.usecase.payment;

import com.licensis.notaire.application.port.in.payment.QueryPaymentsUseCase;
import com.licensis.notaire.application.port.out.payment.PaymentRepositoryPort;
import com.licensis.notaire.domain.payment.PaymentDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Read side of CU47: retrieves registered payments (CU47).
 */
@Service
public class PaymentQueryService implements QueryPaymentsUseCase {

    private final PaymentRepositoryPort payments;

    public PaymentQueryService(PaymentRepositoryPort payments) {
        this.payments = payments;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDetails> findAll() {
        return payments.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentDetails> findById(Integer paymentId) {
        return payments.findById(paymentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDetails> findByBudget(Integer budgetId) {
        return payments.findByBudgetId(budgetId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDetails> findByDateRange(Date startDate, Date endDate) {
        return payments.findByDateRange(startDate, endDate);
    }
}
