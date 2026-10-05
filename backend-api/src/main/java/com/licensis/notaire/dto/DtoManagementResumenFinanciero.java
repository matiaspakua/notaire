package com.licensis.notaire.dto;

import java.math.BigDecimal;

/**
 * Aggregate financial summary of a gestión: total budgeted, total collected
 * and pending balance, summed across every presupuesto linked to its trámites.
 */
public record DtoManagementResumenFinanciero(
        Integer idManagement,
        BigDecimal totalPresupuestado,
        BigDecimal totalCobrado,
        BigDecimal pendingBalance) {
}
