package com.licensis.notaire.business;

/**
 * Budget status vocabulary (issue #1346; Owner default 2026-10-10: the stored codes). The
 * {@code budgets.status} column keeps the code and the UI translates it. Workflow order. The web
 * adapter accepts any letter case on write and answers 400 for anything else.
 */
public enum BudgetStatus {
    BORRADOR,
    PENDIENTE,
    APROBADO,
    RECHAZADO,
    FACTURADO
}
