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
| Issue | #792 | open |
| Use Case | CU15 — Procesar pago | exists |
| Specification | `openspec/changes/persist-metodo-pago-on-pago/` | complete |
| Branch | `fix/792_persist-metodo-pago-on-pago` | created |
| Tasks | `tasks.md` | in-progress (through task 9; 10-12 pending) |
| Commits | 87c3783, 7a35382, a452e4e | done |
| Pull Request | | pending |
| CI run | | pending |
| Merge commit | | pending |
| Release / tag | | pending |
| Smoke test | | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Processing a payment with a payment method | `PagoServiceTest#shouldPersistMetodoPagoWhenProcesarPago` / `PagoIntegrationTest#shouldReturnMetodoPagoOnCreate` | passing |
| Retrieving a payment reflects the stored payment method | `PagoIntegrationTest#shouldReturnStoredMetodoPagoOnGetById` | passing |
| Editing a payment's payment method | `PagoServiceTest#shouldUpdateMetodoPagoWhenEditarPago` / `PagoIntegrationTest#shouldPersistUpdatedMetodoPago` | passing |
| Processing a payment without a payment method | `PagoServiceTest#shouldAllowNullMetodoPagoOnProcesarPago` | passing |
| Editing the payment method of a non-existent payment | `PagoServiceTest#shouldThrowWhenEditingMetodoPagoOfMissingPago` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU15 – Procesar pago.md` | yes | a452e4e |
| `CHANGELOG.md` | yes | a452e4e |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #792, CU15, `proposal.md`, `specs/pagos/spec.md` |
| 2 | Failing tests written, test cases designed | yes | commit 87c3783 (tests written and observed failing before 7a35382 implemented them) |
| 3 | Suite green, coverage held, docs updated | yes | `mvn verify` (1497 tests, 0 failures), `preflight.sh` (15/15), CU15 doc + CHANGELOG updated (a452e4e) |
| 4 | CI green, review approved, no conflicts | pending | not yet pushed / no PR opened |
| 5 | Deployed, smoke test passed, Issue closed | pending | not yet merged or deployed |

## Exceptions

None.
