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
| Issue | #792 | closed |
| Use Case | CU15 — Procesar pago | exists |
| Specification | `openspec/changes/persist-metodo-pago-on-pago/` | complete |
| Branch | `fix/792_persist-metodo-pago-on-pago` | merged |
| Tasks | `tasks.md` | in-progress (Gate 5 items open) |
| Commits | 87c3783, 7a35382, a452e4e, bee85e7 | done |
| Pull Request | [#828](https://github.com/matiaspakua/notaire/pull/828) | merged 2026-08-20T13:48:45Z |
| CI run | `CI`, `Frontend CI`, `PR Validation` green on PR head `bee85e7`; `Playwright E2E — Full Suite` failed, but the same suite fails identically on `main` pre-merge (unrelated `01-first-case-tutorial.spec.ts` flake) | green except pre-existing flake |
| Merge commit | `ad74591` | merged to `main` |
| Release / tag | | pending — CD (`cd.yml`) has not run for `ad74591` yet, no image published to GHCR for this commit |
| Smoke test | | pending — not yet performed |

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
| 4 | CI green, review approved, no conflicts | partial | `CI`, `Frontend CI`, `PR Validation` green on PR head `bee85e7`; `Playwright E2E — Full Suite` failed on the same pre-existing `01-first-case-tutorial.spec.ts` flake also failing on `main` since 2026-08-17; no recorded review approval (`reviews` empty on PR #828, self-merged by `matiaspakua`) |
| 5 | Deployed, smoke test passed, Issue closed | partial | Issue #792 closed and merged to `main` (`ad74591`); CD (`cd.yml`) has not run for this commit yet (last run 2026-08-19 for `fc0c9cd`) and no smoke test has been performed |

## Exceptions

None.
