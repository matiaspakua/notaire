package com.licensis.notaire.application.usecase.payment;

import com.licensis.notaire.application.port.in.payment.BudgetSummary;
import com.licensis.notaire.application.port.in.payment.GetBudgetSummaryUseCase;
import com.licensis.notaire.application.port.in.payment.GetPaymentStatusUseCase;
import com.licensis.notaire.application.port.in.payment.QueryPaymentsUseCase;
import com.licensis.notaire.application.port.out.payment.BudgetDescriptor;
import com.licensis.notaire.application.port.out.payment.BudgetLookupPort;
import com.licensis.notaire.domain.payment.Money;
import com.licensis.notaire.domain.payment.PaymentDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * CU47 "Consultar Pago": assembles the financial summary of a budget.
 *
 * <p>The total is derived as {@code pendingBalance + totalPaid} rather than
 * recomputed from the budget charges, preserving the previous formula with exact
 * {@link BigDecimal} arithmetic (issue #1061).
 */
@Service
public class BudgetSummaryService implements GetBudgetSummaryUseCase {

    private final BudgetLookupPort budgets;
    private final GetPaymentStatusUseCase paymentStatus;
    private final QueryPaymentsUseCase paymentQueries;

    public BudgetSummaryService(BudgetLookupPort budgets, GetPaymentStatusUseCase paymentStatus,
            QueryPaymentsUseCase paymentQueries) {
        this.budgets = budgets;
        this.paymentStatus = paymentStatus;
        this.paymentQueries = paymentQueries;
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetSummary summary(Integer budgetId) {
        BudgetDescriptor descriptor = budgets.findDescriptor(budgetId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Presupuesto no encontrado con ID: " + budgetId));

        BigDecimal pendingBalance = paymentStatus.pendingBalance(budgetId);
        List<PaymentDetails> payments = paymentQueries.findByBudget(budgetId);
        BigDecimal totalPaid = payments.stream()
                .map(PaymentDetails::amount)
                .map(Money::nullToZero)
                .reduce(Money.zero(), BigDecimal::add);

        return new BudgetSummary(
                descriptor.budgetId(),
                descriptor.budgetNumber(),
                descriptor.managementId(),
                descriptor.managementNumber(),
                descriptor.managementHeading(),
                Money.of(pendingBalance.add(totalPaid)),
                pendingBalance,
                payments);
    }
}
