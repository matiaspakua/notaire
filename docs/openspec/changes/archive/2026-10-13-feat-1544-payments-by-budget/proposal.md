# Consult one budget's payments, summary and balance on the payments page (#1544)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1544 |
| Use Case | CU47 – Consultar pago; CU15 – Registrar pago |
| Branch | `feat/1544_payments_by_budget` |
| Gate 1 status | draft |

## Objetivo

CU47 step 6 asks for a budget's total, balance, payment status and payments. `/dashboard/payments` only listed every payment with `#idBudget`. `GET /payments/budget/{id}` and `/balance` existed but had no UI caller (#1250 bucket C).

## What Changes

- Payments page: a 'Consultar por presupuesto' `BudgetPicker` kept in the URL (`?budget=`), clearable.
- With a budget selected: a summary (total, paid, balance from `/balance`, status from `/status`) and only that budget's payments, newest first; empty state 'Este presupuesto no tiene pagos'.
- Hooks `usePaymentsByBudget` and `useBudgetBalance`; i18n `pagos.consulta.*` (es/en).
- Both endpoints removed from `contracts/api-reachability-allowlist.yaml`.
- Tests: Vitest `payments-by-budget.test.tsx` (7), Playwright TS-0014 `CU47-BUDGET-01/02`. CHANGELOG.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Paid = total − outstanding balance | CU47 step 6; PaymentStatus | Existing rule, now visible |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `payment-consult`: Consulting a budget's payments in the UI.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | payments page, hooks, i18n |
| `contracts` | yes | allowlist |
| `testing/e2e` | yes | TS-0014 |

### Surface area

- Endpoints: none new; `GET /payments/budget/{id}` and `/balance` gain a UI caller
- Entities / Flyway / configuration: none

### Architecture review

Frontend only, reusing existing endpoints and the server-search BudgetPicker.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Added entry |
| `contracts/api-reachability-allowlist.yaml` | two bucket C entries removed |
