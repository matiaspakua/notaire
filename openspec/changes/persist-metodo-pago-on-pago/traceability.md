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
| Specification | `openspec/changes/persist-metodo-pago-on-pago/` | in-progress |
| Branch | `fix/792_persist-metodo-pago-on-pago` | created |
| Tasks | `tasks.md` | in-progress |
| Commits | | pending |
| Pull Request | | pending |
| CI run | | pending |
| Merge commit | | pending |
| Release / tag | | pending |
| Smoke test | | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Processing a payment with a payment method | `PagoServiceTest#shouldPersistMetodoPagoWhenProcesarPago` / `PagoIntegrationTest#shouldReturnMetodoPagoOnCreate` | pending |
| Retrieving a payment reflects the stored payment method | `PagoIntegrationTest#shouldReturnStoredMetodoPagoOnGetById` | pending |
| Editing a payment's payment method | `PagoServiceTest#shouldUpdateMetodoPagoWhenEditarPago` / `PagoIntegrationTest#shouldPersistUpdatedMetodoPago` | pending |
| Processing a payment without a payment method | `PagoServiceTest#shouldAllowNullMetodoPagoOnProcesarPago` | pending |
| Editing the payment method of a non-existent payment | `PagoServiceTest#shouldThrowWhenEditingMetodoPagoOfMissingPago` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/01-business/02-use-cases/03_CU - Casos de Uso/CU15 – Procesar pago.md` | no | |
| `CHANGELOG.md` | no | |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | |
| 2 | Failing tests written, test cases designed | pending | |
| 3 | Suite green, coverage held, docs updated | pending | |
| 4 | CI green, review approved, no conflicts | pending | |
| 5 | Deployed, smoke test passed, Issue closed | pending | |

## Exceptions

None.
