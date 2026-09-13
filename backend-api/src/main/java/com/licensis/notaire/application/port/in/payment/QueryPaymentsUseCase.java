package com.licensis.notaire.application.port.in.payment;

import com.licensis.notaire.domain.payment.PaymentDetails;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Inbound port for the read side of CU47 "Consultar Estado de Pago": retrieving
 * registered payments.
 *
 * <p>Grouped as one port because these are the same capability (reading payments)
 * viewed through different filters; splitting them would be over-porting.
 */
public interface QueryPaymentsUseCase {

    List<PaymentDetails> findAll();

    Optional<PaymentDetails> findById(Integer paymentId);

    List<PaymentDetails> findByBudget(Integer budgetId);

    List<PaymentDetails> findByDateRange(Date startDate, Date endDate);
}
