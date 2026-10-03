# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1061 | open (IN PROGRESS label blocked: `gh` write denied for this agent) |
| Use Case | CU15 – Procesar pago | exists on issue |
| Specification | `openspec/changes/fix-1061-money-bigdecimal/` | Gate 1 passed |
| Branch | `cursor/fix-1061-money-bigdecimal-69d3` | created (cloud prefix; Constitution form would be `fix/1061_money_bigdecimal`) |
| Tasks | `tasks.md` | in progress |
| Commits | `52312892` | pushed |
| Pull Request | [#1126](https://github.com/matiaspakua/notaire/pull/1126) | draft |
| CI run | pending | subscribed |
| Merge commit | | pending |
| Release / tag | | pending |
| Smoke test | | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Charge lines 0.1 + 0.2 sum exactly to 0.3 | `BudgetChargesTest#shouldSumTenthsExactlyWithoutFloatError` | pending |
| Overpay of pending balance 0.3 after 0.1+0.2 is rejected | `ProcessPaymentServiceTest#shouldRejectOverpaymentWhenBalanceIsExactTenths` | pending |
| Payment entity / DTOs expose BigDecimal amount | `PaymentEntityTest` / DTO unit coverage | pending |
| Flyway money columns are NUMERIC with scale | `FlywaySchemaValidationIntegrationTest` / V39 | pending |
| Receipt formats amount from BigDecimal scale 2 | `ReportServicePaymentReceiptTest` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| CU15 use case (decimal money note) | pending | |
| `CHANGELOG.md` | pending | |
| OpenAPI payment amount schemas | pending | |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `openspec validate --strict` + `validate-sdlc-plan.sh` exit 0 |
| 2 | Failing tests written, test cases designed | yes | `BudgetChargesTest#shouldSumTenthsExactlyWithoutFloatError` failed under float (`30.3` vs `30.300001`); then BigDecimal |
| 3 | Suite green, coverage held, docs updated | pending | |
| 4 | CI green, review approved, no conflicts | pending | |
| 5 | Deployed, smoke test passed, Issue closed | pending | |

## Exceptions

Label `in-progress` could not be applied (`gh` GraphQL: Resource not accessible by integration); recorded here, not treated as a Constitution §12 process exception. Cloud branch naming uses `cursor/fix-1061-money-bigdecimal-69d3` as required by the agent platform.
