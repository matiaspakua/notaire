package com.licensis.notaire.dto;

import java.math.BigDecimal;

import java.util.List;

/**
 * CU47 financial summary for a presupuesto: its gestión, total, pending
 * balance, and the list of payments applied to it.
 */
public record DtoBudgetResumen(
        Integer idBudget,
        int numberBudget,
        Integer idManagement,
        Integer numberManagement,
        String encabezadoManagement,
        BigDecimal total,
        BigDecimal pendingBalance,
        List<DtoPaymentResponse> payments) {
}
