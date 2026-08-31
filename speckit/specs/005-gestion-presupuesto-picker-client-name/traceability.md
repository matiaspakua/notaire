# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue #889 → spec.md → plan.md → tasks.md → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #889 | open |
| Use Case | CU02 — Iniciar Gestión | exists |
| Specification | `speckit/specs/005-gestion-presupuesto-picker-client-name/` | active |
| Branch | `fix/889_presupuesto-picker-shows-client-name` | in progress |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | none planned — continuous deploy off `main`, same as #879/#883 | n/a |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| US1 scenario 1 — presupuesto with cliente shows cliente in picker | `TS-0011-gestiones-crud-workflow.spec.ts#CU02-GW05` | passing |
| US1 scenario 2 — presupuesto without cliente keeps id-only label | covered by unchanged `CU02-GW01`/`CU19-GW01` (exercise pickers with seed presupuesto, no `persona`) | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | pending | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + spec.md + Acceptance Criteria | yes | Issue #889, `spec.md` Notaire Traceability + Given/When/Then scenarios, `bash scripts/validate-speckit-plan.sh 005-gestion-presupuesto-picker-client-name` passing |
| 2 | Failing tests written, test cases designed | yes | `CU02-GW05` written first (TDD), confirmed red against pre-fix code, then green after the rendering fix |
| 3 | Suite green, coverage held, docs updated | pending | tsc/lint clean, Playwright green; `CHANGELOG.md` pending |
| 4 | CI green, review approved, no conflicts | pending | |
| 5 | Deployed, smoke test passed, Issue closed | pending | |

## Exceptions

None taken.
