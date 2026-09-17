package com.licensis.notaire.application.port.in.payment;

import com.licensis.notaire.domain.payment.PaymentDetails;

import java.util.List;

/**
 * Output of {@link GetBudgetSummaryUseCase} (CU47): the transport-agnostic financial
 * summary of a budget. The inbound web adapter maps this onto {@code DtoBudgetResumen}.
 *
 * @param budgetId         budget identifier
 * @param budgetNumber     business number of the budget
 * @param managementId     identifier of the owning management, {@code null} when unassigned
 * @param managementNumber number of the owning management, {@code null} when unassigned
 * @param managementHeading heading of the owning management, {@code null} when unassigned
 * @param total            total value of the budget
 * @param pendingBalance   remaining balance
 * @param payments         payments applied to the budget
 */
public record BudgetSummary(
        Integer budgetId,
        int budgetNumber,
        Integer managementId,
        Integer managementNumber,
        String managementHeading,
        float total,
        float pendingBalance,
        List<PaymentDetails> payments) {

    public BudgetSummary {
        payments = payments == null ? List.of() : List.copyOf(payments);
    }
}
