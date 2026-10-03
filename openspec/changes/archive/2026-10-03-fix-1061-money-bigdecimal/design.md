> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1061 (audit-2026-09, priority:high) and the owner follow-up require all
monetary `float`/`Float` fields — not only `Payment.amount` — to become
`BigDecimal`, with one Flyway pass to `NUMERIC`. CU15 overpayment (#848) and
budget totals (CU45) currently tolerate float error; classic `0.1 + 0.2` can
break the pending-balance guard.

## Goals / Non-Goals

**Goals:**

- Exact scale-2 money arithmetic in domain (`BudgetCharges`, payment status,
  process/edit payment).
- Entities + shared DTOs + REST money params/bodies use `BigDecimal`.
- Single Flyway `V39` converting remaining `real` money columns and tightening
  unconstrained `NUMERIC` money/percentage columns.
- Unit tests that fail on float today (`0.1 + 0.2`) and pass with BigDecimal.
- Receipt formatting from BigDecimal.

**Non-Goals:**

- Changing `WorkflowNode.positionX` / `positionY`.
- Frontend rewrite to decimal strings; keep JSON numbers.
- Recovering historically corrupted float cents already in the DB.
- Touching `local-ai/`.

## Decisions

- **Currency scale `NUMERIC(19,2)` / Java scale 2, `HALF_UP`** for all currency
  amounts; **`NUMERIC(7,4)`** for `variable_percentage`.
- **Preserve `BudgetCharges` compounding order** (percentage over running total)
  using `BigDecimal` multiply/divide with scale 2 after each step so legacy
  order stays but results are exact at cents.
- **Jackson maps JSON numbers to `BigDecimal`** — no string-only money API.
- **One coordinated PR** covering concepts, items, budgets, properties,
  documents, registration drafts, and payments (matches owner comment).
- **Null money**: nullable DB/entity fields stay nullable `BigDecimal`;
  primitives become non-null `BigDecimal` with zero defaults where the old
  primitive `float` defaulted to `0f`.

## Riesgos / Trade-offs

- [Risk] `real` → `NUMERIC(19,2)` is lossy for already-polluted float rows →
  Mitigation: document residual risk; `USING column::numeric` + round to scale 2.
- [Risk] Broad compile blast across DTOs/tests → Mitigation: convert domain +
  entities + DTOs together; fix tests in the same PR.
- [Risk] Frontend client-side float sums may diverge → Mitigation: keep
  overpay enforcement server-side only.
- [Risk] Cannot apply `in-progress` label via this agent's `gh` token →
  Mitigation: record in traceability Exceptions.

## Testing Strategy

TDD: add exact-decimal domain/service tests that fail under float; then convert
types; observe pass.

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| 0.1 + 0.2 == 0.3 on charge lines | unit | `BudgetChargesTest#shouldSumTenthsExactlyWithoutFloatError` |
| Overpay when balance is exact tenths | unit | `ProcessPaymentServiceTest#shouldRejectOverpaymentWhenBalanceIsExactTenths` |
| Payment amount is BigDecimal | unit | `PaymentEntityTest` / DTO tests |
| Schema NUMERIC after V39 | integration | `FlywaySchemaValidationIntegrationTest` (`-Ppg-integration`) |
| Receipt formats BigDecimal amount | unit | `ReportServicePaymentReceiptTest` |

- New unit tests: above + PaymentStatus BigDecimal comparisons
- Coverage impact: should hold JaCoCo ratchet (touched paths tested)

## Regression Strategy

- Existing tests affected: all payment/budget/concept/item/property/document-cost
  tests using `float` literals; Bruno payment over-limit; Playwright CU15 if
  amounts assert float text.
- Full suite: `mvn verify -pl backend-api -am`
- HTTP/Bruno: `backend-api/api-test/payments/` when stack up
- Legacy `jpa` / Swing: out of scope except shared DTO types

## Playwright Strategy

- n/a for new UI surface — no frontend type change required. Existing CU15 E2E
  (`TS-0014-pagos-workflow.spec.ts`) remains the regression guard for saldo /
  overpay messaging; run in CI. Record "n/a — no UI surface change; existing
  CU15 E2E remains regression" in tasks §7.

## Deployment Strategy

- Flyway migration required: **yes — V39**
- Deployment order: apply migration with backend roll (normal); brief lock on
  ALTER COLUMN for money tables
- Configuration / `.env`: none
- Feature flag: no
- Smoke test: `GET /actuator/health`; create payment with amount `0.1` then
  `0.2` against total `0.3`; confirm third micro-payment 409

## Rollback Strategy

- Revert safe: code revert requires a new forward migration to restore `real`
  (do not edit V39); prefer hot-fix over schema downgrade
- Database rollback: new `V40` only if emergency; accept lossy round-trip
- Data written under new behavior: exact cents going forward
- Blast radius if delayed: clients still send JSON numbers — compatible

## Migration Plan

1. Gate 1 artifacts + failing exact-decimal tests
2. Domain/ports/services → BigDecimal
3. Entities + shared DTOs + controllers
4. Flyway V39 + schema validation
5. Receipt formatting + docs/CHANGELOG
6. Preflight, PR, CI, merge

## Open Questions

None — precision/scale and rounding locked above per prep survey.
