# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue #889 → spec.md → plan.md → tasks.md → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #889 | closed |
| Use Case | CU02 — Iniciar Gestión | exists |
| Specification | `speckit/specs/archive/005-gestion-presupuesto-picker-client-name/` | archived |
| Branch | `fix/889_presupuesto-picker-shows-client-name` | merged |
| Tasks | `tasks.md` | done |
| Commits | `d79405a`, `b87cdbd`, `9f7e034` | done |
| Pull Request | [#890](https://github.com/matiaspakua/notaire/pull/890) | merged |
| CI run | `Frontend CI`, `PR Validation`, `CI - Build, Test & Security`, `Playwright E2E — Full Suite` (397 passed) — all green on head `9f7e034` | passed |
| Merge commit | `f3d72f6` on `main` | done |
| Release / tag | none planned — continuous deploy off `main`, same as #879/#883 | n/a |
| Smoke test | Frontend-only rendering change, no backend/DB change. `Playwright E2E — Full Suite` (incl. `CU02-GW05`, the exact acceptance scenario) ran green against the merged content on PR head `9f7e034`, content-identical to squash-merge commit `f3d72f6` on `main`. No separate Docker-stack re-run performed — not required for a pure client-side label change already exercised end-to-end. | passed |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| US1 scenario 1 — presupuesto with cliente shows cliente in picker | `TS-0011-gestiones-crud-workflow.spec.ts#CU02-GW05` | passing |
| US1 scenario 2 — presupuesto without cliente keeps id-only label | covered by unchanged `CU02-GW01`/`CU19-GW01` (exercise pickers with seed presupuesto, no `persona`) | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | `b87cdbd` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + spec.md + Acceptance Criteria | yes | Issue #889, `spec.md` Notaire Traceability + Given/When/Then scenarios, `bash scripts/validate-speckit-plan.sh 005-gestion-presupuesto-picker-client-name` passing |
| 2 | Failing tests written, test cases designed | yes | `CU02-GW05` written first (TDD), confirmed red against pre-fix code, then green after the rendering fix |
| 3 | Suite green, coverage held, docs updated | yes | `npx tsc --noEmit` + `npm run lint` clean, `bash scripts/run_pipeline.sh` green (docker stack up, `preflight --full` — 18 passed/1 skipped/0 failed including full Playwright suite 397 passed, Bruno API tests, Docker build/smoke test — and `markdown-lint`), `CHANGELOG.md` updated |
| 4 | CI green, review approved, no conflicts | yes | PR #890, all CI checks green, merged as `f3d72f6` via squash merge, no conflicts |
| 5 | Deployed, smoke test passed, Issue closed | yes | Merged to `main` (continuous deploy), smoke test evidence above, Issue #889 closed (`COMPLETED`) by merge |

## Exceptions

None taken.
