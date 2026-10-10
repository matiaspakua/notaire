package com.licensis.notaire.dto;

import java.math.BigDecimal;

/**
 * Aggregate financial summary of a management: total budgeted, total collected
 * and pending balance, summed across every budget linked to its procedures.
 */
public record DtoManagementFinancialSummary(
        Integer idManagement,
        BigDecimal totalPresupuestado,
        BigDecimal totalCobrado,
        BigDecimal pendingBalance) {
}
