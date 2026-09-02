# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #848 | open |
| Use Case | CU15 – Procesar pago (#168) / RF-18 "Abonar trámite" (#20) | exists |
| Specification | `openspec/changes/pago-limite-saldo-pendiente/` | in progress |
| Branch | `fix/848_reject-payments-exceeding-saldo` | created |
| Tasks | `tasks.md` | complete |
| Commits | pending (not yet committed) | pending |
| Pull Request | | pending |
| CI run | | pending |
| Merge commit | | pending |
| Release / tag | | pending |
| Smoke test | passed locally (Bruno suite + Playwright SALDO-05) | passed |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Payment within the saldo pendiente is accepted | `PagoServiceTest.shouldProcessValidPago` (unit) | passed |
| Payment exceeding the saldo pendiente is rejected | `PagoServiceTest` / `PagoControllerTest.shouldReturn409WhenCreateExceedsSaldo` / `.shouldReturn409OnParamsExceedsSaldo` (HTTP 409) | passed |
| Saldo pendiente calculation already accounts for prior payments | `PagoServiceIntegrationTest.shouldRejectPagoExceedingSaldoReducedByPriorPayment` (integration) | passed |
| Payment exceeding saldo is rejected and not persisted | `PagoServiceIntegrationTest.shouldRejectPagoExceedingSaldoPendiente` (integration) | passed |
| Operator sees a specific message when a payment is rejected for exceeding saldo | `frontend/tests/e2e/TS-0014-pagos-saldo-picker.spec.ts::CU15-SALDO-05 (#848)` (Playwright E2E) | passed |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU15 – Procesar pago.md` | yes (paso 11 + exception 11.1) | pending commit |
| `CHANGELOG.md` | yes (Fixed entry, issue #848) | pending commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #848; proposal/design/tasks/specs written under `openspec/changes/pago-limite-saldo-pendiente/` |
| 2 | Failing tests written, test cases designed | yes | Unit/integration/E2E tests written and passing per Requirement coverage above |
| 3 | Suite green, coverage held, docs updated | yes | `mvn verify -pl backend-api` BUILD SUCCESS (JaCoCo ratchet floor held; 5 failures + 1 error in unrelated pre-existing tests: `BusinessWorkflowIntegrationTest`, `GestionArchiveIntegrationTest`, `RemainingControllersIntegrationTest`, `SimpleControllersTest` — confirmed pre-existing via `git stash` on `main`, same failures before this change); Checkstyle clean; SpotBugs violations all pre-existing/unrelated (legacy `negocio`/`service` classes); CU15 use case + CHANGELOG updated |
| 4 | CI green, review approved, no conflicts | pending | PR not yet opened |
| 5 | Deployed, smoke test passed, Issue closed | pending | Local smoke test passed (Bruno suite, Playwright SALDO-05); production deploy pending merge |

## Exceptions

None.
