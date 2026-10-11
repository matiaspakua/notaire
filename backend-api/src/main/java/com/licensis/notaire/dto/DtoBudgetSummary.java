package com.licensis.notaire.dto;

import java.math.BigDecimal;

import java.util.List;

/**
 * CU47 financial summary for a budget: its management, total, pending
 * balance, and the list of payments applied to it.
 */
public record DtoBudgetSummary(
        Integer idBudget,
        int numberBudget,
        Integer idManagement,
        Integer numberManagement,
        String managementHeading,
        BigDecimal total,
        BigDecimal pendingBalance,
        List<DtoPaymentResponse> payments) {
}
