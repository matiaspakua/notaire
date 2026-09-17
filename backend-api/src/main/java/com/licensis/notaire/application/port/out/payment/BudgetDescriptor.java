package com.licensis.notaire.application.port.out.payment;

/**
 * Identity of a budget plus the management it belongs to, as needed by the CU47
 * financial summary. Management fields are {@code null} when the budget has no
 * procedure linking it to a management.
 *
 * @param budgetId          budget identifier
 * @param budgetNumber      business number of the budget
 * @param managementId      identifier of the owning management, may be {@code null}
 * @param managementNumber  number of the owning management, may be {@code null}
 * @param managementHeading heading of the owning management, may be {@code null}
 */
public record BudgetDescriptor(
        Integer budgetId,
        int budgetNumber,
        Integer managementId,
        Integer managementNumber,
        String managementHeading) {
}
