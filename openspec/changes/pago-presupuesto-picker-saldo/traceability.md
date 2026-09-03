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
| Issue | #796 | open (in-progress) |
| Use Case | CU15 – Procesar pago (#168), pasos 2-5 y 11 | exists |
| Specification | `openspec/changes/pago-presupuesto-picker-saldo/` | in progress |
| Branch | `fix/796_pago-presupuesto-picker-saldo-regression` | created |
| Tasks | `tasks.md` | see reconciliation note — feature already shipped on `main`; this branch fixes the flaky E2E coverage |
| Commits | `2ae786af`, plus this branch's `02-demo-two-full-cases.spec.ts` picker fix | in progress |
| Pull Request | [#927](https://github.com/matiaspakua/notaire/pull/927) | open |
| CI run | UI E2E job was red due to unrelated/pre-existing failures (`TS-0012` → fixed separately in #929; `02-demo-two-full-cases.spec.ts` → fixed on this branch, see below) | in progress |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Operator selects a presupuesto from the picker | `TS-0014-pagos-saldo-picker.spec.ts#CU15-SALDO-01/02` | passing |
| Operator picks a presupuesto and it becomes the payment target | `TS-0014-pagos-saldo-picker.spec.ts#CU15-SALDO-02` | passing |
| Saldo pendiente is shown after selecting a presupuesto | `TS-0014-pagos-saldo-picker.spec.ts#CU15-SALDO-03` | fixed — was flaky on ambiguous locator, now uses `data-testid="saldo-pendiente-amount"` |
| Saldo pendiente updates when the selection changes | `TS-0014-pagos-saldo-picker.spec.ts#CU15-SALDO-04` | fixed — same locator fix |
| Overpayment rejected with saldo pendiente message | `TS-0014-pagos-saldo-picker.spec.ts#CU15-SALDO-05` | passing |
| Demo script (`02-demo-two-full-cases.spec.ts`) still completes a pago after the numeric-ID input was replaced by the picker | `02-demo-two-full-cases.spec.ts` | fixed — updated the Pago step to drive `select-presupuesto-pago` instead of the removed `presupuesto id` field |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU15 – Procesar pago.md` | yes | pending (this branch) |
| `CHANGELOG.md` | yes | pending (this branch) |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #796 open, linked to CU15; spec/proposal drafted |
| 2 | Failing tests written, test cases designed | yes | `TS-0014-pagos-saldo-picker.spec.ts` pre-existed; failure mode confirmed (strict-mode locator ambiguity) before fixing |
| 3 | Suite green, coverage held, docs updated | yes | 5/5 `TS-0014-pagos-saldo-picker.spec.ts` passing locally against the full Docker stack; CU15 doc + CHANGELOG updated |
| 4 | CI green, review approved, no conflicts | pending | |
| 5 | Deployed, smoke test passed, Issue closed | pending | |

## Exceptions

None.
