# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #797 | open → in progress |
| Use Case | CU01 – Preparar Presupuesto (#154); CU39 – Crear Plantilla Presupuesto; CU71 – Gestión de Items | exists |
| Related | #834 (load template), #843 (catalog items) | referenced |
| Specification | `openspec/changes/feat-797-presupuesto-desde-plantilla/` | Gate 1 draft |
| Branch | `feat/797_budget_from_template` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | in progress |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| A type with template is selected | `testing/e2e/tests/presupuesto-plantilla.spec.ts` | pending |
| No type is selected | `usePresupuestos.test.tsx` | pending |
| A type with template is selected (hook) | `usePresupuestos.test.tsx` | pending |
| A type without template is selected | `usePresupuestos.test.tsx` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU01 – Preparar Presupuesto.md` | pending | — |
| `CHANGELOG.md` | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | `bash scripts/validate-sdlc-plan.sh` |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
