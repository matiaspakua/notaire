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
| Issue | #981 | in-progress |
| Use Case | CU82 — Generar Minuta de Inscripción | exists (`docs/100-business/102-use-cases/CU82 – Generar Minuta de Inscripción.md`) |
| Specification | `openspec/changes/fix-procedure-nested-fk-hydration/` | in progress |
| Branch | `fix/981_procedure_nested_fk_hydration` | not yet created |
| Tasks | `tasks.md` | not yet written |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Creating a Procedure with a real Deed id returns the Deed's actual state | new integration test in `ProcedureControllerFkHydrationIntegrationTest` (or similar) | pending |
| A subsequent GET reflects the same real association state | same test class | pending |
| idProcedureType is required | same test class | pending |
| A non-existent referenced id is rejected | same test class | pending |
| Updating a Procedure with a real Deed id returns the Deed's actual state | same test class | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| None expected — this is a bug fix to an internal contract, not a business-rule or Use Case change | no | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | proposal.md + spec.md written |
| 2 | Failing tests written, test cases designed | pending | |
| 3 | Suite green, coverage held, docs updated | pending | |
| 4 | CI green, review approved, no conflicts | pending | |
| 5 | Deployed, smoke test passed, Issue closed | pending | |

## Exceptions

None taken.
