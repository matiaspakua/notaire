package com.licensis.notaire.application.usecase.payment;

import com.licensis.notaire.application.port.in.payment.ProcessPaymentCommand;
import com.licensis.notaire.application.port.in.payment.ProcessPaymentUseCase;
import com.licensis.notaire.application.port.out.payment.BudgetLookupPort;
import com.licensis.notaire.application.port.out.payment.NewPayment;
import com.licensis.notaire.application.port.out.payment.PaymentRepositoryPort;
import com.licensis.notaire.domain.payment.BudgetCharges;
import com.licensis.notaire.domain.payment.Money;
import com.licensis.notaire.domain.payment.PaymentDetails;
import com.licensis.notaire.exception.PendingBalanceExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

/**
 * CU15 "Procesar Payment" use case: validates a payment against the budget's pending
 * balance and registers it.
 *
 * <p>Pure orchestration — the arithmetic lives in {@link BudgetCharges}, the I/O behind
 * the outbound ports. The Spring annotations only declare wiring and the transaction
 * boundary; no other framework type reaches this class.
 */
@Service
public class ProcessPaymentService implements ProcessPaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessPaymentService.class);

    private final PaymentRepositoryPort payments;
    private final BudgetLookupPort budgets;

    public ProcessPaymentService(PaymentRepositoryPort payments, BudgetLookupPort budgets) {
        this.payments = payments;
        this.budgets = budgets;
    }

    @Override
    @Transactional
    public PaymentDetails process(ProcessPaymentCommand command) {
        Integer budgetId = command.budgetId();
        BigDecimal amount = Money.of(command.amount());
        log.info("Procesando payment for budget {}: monto={}", budgetId, amount);

        BudgetCharges charges = budgets.findCharges(budgetId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Budget not found with ID: " + budgetId));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("The payment amount must be greater than zero");
        }

        BigDecimal pendingBalance = charges.pendingBalanceAfter(payments.sumAmountByBudgetId(budgetId));
        log.info("Outstanding balance for budget {}: {}", budgetId, pendingBalance);

        if (amount.compareTo(pendingBalance) > 0) {
            throw new PendingBalanceExceededException(
                    String.format("The payment amount ($%.2f) cannot exceed the outstanding balance ($%.2f)",
                            amount, pendingBalance));
        }

        PaymentDetails saved = payments.register(new NewPayment(
                budgetId,
                amount,
                command.date() != null ? command.date() : new Date(),
                command.notes(),
                command.paymentMethod()));

        log.info("Payment registrado exitosamente: ID={}", saved.id());
        return saved;
    }
}
