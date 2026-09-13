package com.licensis.notaire.application.usecase.payment;

import com.licensis.notaire.application.port.in.payment.GetPaymentStatusUseCase;
import com.licensis.notaire.application.port.out.payment.BudgetLookupPort;
import com.licensis.notaire.application.port.out.payment.PaymentRepositoryPort;
import com.licensis.notaire.domain.payment.BudgetCharges;
import com.licensis.notaire.domain.payment.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CU47 "Consultar Estado de Pago": derives the pending balance and the aggregated
 * status of a budget from its charges and the amount already paid.
 */
@Service
public class PaymentStatusService implements GetPaymentStatusUseCase {

    private static final Logger log = LoggerFactory.getLogger(PaymentStatusService.class);

    private final PaymentRepositoryPort payments;
    private final BudgetLookupPort budgets;

    public PaymentStatusService(PaymentRepositoryPort payments, BudgetLookupPort budgets) {
        this.payments = payments;
        this.budgets = budgets;
    }

    @Override
    @Transactional(readOnly = true)
    public float pendingBalance(Integer budgetId) {
        float pendingBalance = charges(budgetId).pendingBalanceAfter(payments.sumAmountByBudgetId(budgetId));
        log.debug("Saldo pendiente para presupuesto {}: {}", budgetId, pendingBalance);
        return pendingBalance;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentStatus status(Integer budgetId) {
        BudgetCharges charges = charges(budgetId);
        Float totalPaid = payments.sumAmountByBudgetId(budgetId);
        return PaymentStatus.of(totalPaid, charges.pendingBalanceAfter(totalPaid));
    }

    private BudgetCharges charges(Integer budgetId) {
        return budgets.findCharges(budgetId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Presupuesto no encontrado con ID: " + budgetId));
    }
}
