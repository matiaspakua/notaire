# Fix money stored as float — use BigDecimal (CU15)

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1061 |
| Use Case | CU15 – Procesar pago |
| Branch | `cursor/fix-1061-money-bigdecimal-69d3` |
| Gate 1 status | pending |

## Objetivo

Monetary amounts across payments, budgets, concepts, items, properties, and related
entities are stored and computed as `float`/`Float`. Binary floating point breaks
exact comparisons required by CU15 step 11 (monto ≤ saldo pendiente) and installment
totals (classic `0.1 + 0.2` error). This change migrates all monetary fields to
`BigDecimal` (scale 2) and DB columns to `NUMERIC`, preserving overpayment rejection
(#848) with exact arithmetic.

## What Changes

- Convert monetary Java types from `float`/`Float` to `BigDecimal` on entities,
  shared DTOs, payment domain (`ChargeLine`, `BudgetCharges`, `PaymentDetails`,
  status/summary), ports, services, controllers, and report receipt formatting.
- Add Flyway `V39` altering remaining `real` money columns to `NUMERIC(19,2)`
  (and tightening already-`NUMERIC` money columns where needed; percentage uses
  `NUMERIC(7,4)`).
- Add unit tests that pin exact decimal sums (`0.1 + 0.2 == 0.3`) and near-boundary
  overpayment checks.
- Keep JSON numbers as numbers (Jackson → `BigDecimal`); frontend stays on TS
  `number`. Do not convert UI layout floats (`WorkflowNode.positionX`/`Y`).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Monto del pago MUST be ≤ saldo pendiente (exact, scale 2) | CU15 step 11 / #848 | Changed (float → BigDecimal) |
| Budget total / pending balance / installment sums MUST be exact at scale 2 | CU15 / CU45 / CU47 | Changed |
| Monetary persistence MUST use NUMERIC with explicit scale | Issue #1061 owner scope | New |
| Layout coordinates MUST remain float (`positionX`/`Y`) | Non-goal | Made explicit |

## Capabilities

### New Capabilities

- `money-bigdecimal`: Monetary amounts use `BigDecimal` end-to-end (API, domain,
  persistence) with scale-2 currency arithmetic and NUMERIC columns.

### Modified Capabilities

None at the permanent-spec requirement level beyond this new capability.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Entities, domain, ports, services, adapters, Flyway V39, tests |
| `notaire-shared` | yes | Money fields on DTOs → `BigDecimal` |
| `frontend` | no | JSON numbers unchanged; E2E still valid |
| `frontend-swing` | no | Removed; out of scope |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: Payment, Concept, Item, Budget, Property, SubmittedDocument,
  DocumentCostTemplate, RegistrationDraft
- Endpoints: `/api/v1/pagos` (body + params), balance/status; other controllers
  exposing money fields (concepts, items, budgets, properties, cost templates)
- Database (Flyway `V39`): `payments.amount`, `concepts.amount`, `items.amount`,
  `budgets.property_amount`, `properties.fiscal_valuation`,
  `submitted_documents.amount_to_pay`, `document_types.amount_to_pay`,
  `document_cost_templates.fixed_amount` / `variable_percentage`,
  `registration_drafts.operation_price`
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Incremental hardening of the existing hexagonal payment stack (ADR-021) and
legacy entities. No new ADR — type/precision fix only; arithmetic order of
`BudgetCharges` percentage compounding is preserved with `BigDecimal`.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU15 – Procesar pago.md` | Note exact decimal money / BigDecimal |
| `CHANGELOG.md` | Unreleased: monetary amounts use BigDecimal / NUMERIC |
| OpenAPI annotations on payment (and related) controllers | Schema types reflect decimal amounts |

## Out of Scope

- `WorkflowNode.positionX` / `positionY` (layout coordinates, not money).
- Frontend UX rewrite to decimal strings.
- Recovering exact cents lost historically by float storage (document residual risk).
- Product features beyond type/precision fix.
