package com.licensis.notaire.dto;

/**
 * Aggregate financial summary of a gestión: total budgeted, total collected
 * and pending balance, summed across every presupuesto linked to its trámites.
 */
public record DtoGestionResumenFinanciero(
        Integer idGestion,
        Float totalPresupuestado,
        Float totalCobrado,
        Float saldoPendiente) {
}
