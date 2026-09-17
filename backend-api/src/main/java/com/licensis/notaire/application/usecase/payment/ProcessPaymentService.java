package com.licensis.notaire.application.usecase.payment;

import com.licensis.notaire.application.port.in.payment.ProcessPaymentCommand;
import com.licensis.notaire.application.port.in.payment.ProcessPaymentUseCase;
import com.licensis.notaire.application.port.out.payment.BudgetLookupPort;
import com.licensis.notaire.application.port.out.payment.NewPayment;
import com.licensis.notaire.application.port.out.payment.PaymentRepositoryPort;
import com.licensis.notaire.domain.payment.BudgetCharges;
import com.licensis.notaire.domain.payment.PaymentDetails;
import com.licensis.notaire.exception.PendingBalanceExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * CU15 "Procesar Pago" use case: validates a payment against the budget's pending
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
        Float amount = command.amount();
        log.info("Procesando pago para presupuesto {}: monto={}", budgetId, amount);

        BudgetCharges charges = budgets.findCharges(budgetId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Presupuesto no encontrado con ID: " + budgetId));

        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor a cero");
        }

        float pendingBalance = charges.pendingBalanceAfter(payments.sumAmountByBudgetId(budgetId));
        log.info("Saldo pendiente para presupuesto {}: {}", budgetId, pendingBalance);

        if (amount > pendingBalance) {
            throw new PendingBalanceExceededException(
                    String.format("El monto del pago ($%.2f) no puede exceder el saldo pendiente ($%.2f)",
                            amount, pendingBalance));
        }

        PaymentDetails saved = payments.register(new NewPayment(
                budgetId,
                amount,
                command.date() != null ? command.date() : new Date(),
                command.notes(),
                command.paymentMethod()));

        log.info("Pago registrado exitosamente: ID={}", saved.id());
        return saved;
    }
}
