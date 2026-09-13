package com.licensis.notaire.application.port.in.payment;

/**
 * Inbound port for CU47 "Consultar Pago": the financial summary of a budget
 * (total, pending balance and applied payments) together with the management it
 * belongs to.
 */
public interface GetBudgetSummaryUseCase {

    /**
     * @throws IllegalArgumentException if the budget does not exist
     */
    BudgetSummary summary(Integer budgetId);
}
