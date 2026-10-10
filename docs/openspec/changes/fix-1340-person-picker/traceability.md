# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1340 | open → in progress |
| Use Case | CU01 – Preparar presupuesto; CU02 – Iniciar gestión; CU19 – Consultar gestiones por cliente; CU39 – Cargar ítems desde la plantilla | exists |
| Specification | `docs/openspec/changes/fix-1340-person-picker/` | Gate 1 draft |
| Branch | `fix/1340_person_picker` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | pushed |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| New person found by name | `testing/e2e/tests/TS-0111-person-picker.spec.ts` | passing |
| Client filter | `testing/e2e/tests/TS-0111-person-picker.spec.ts` | passing |
| Combobox semantics | `frontend/src/tests/unit/person-picker.test.tsx` | passing |
| No size=1000 anywhere | `frontend/src/tests/unit/person-picker.test.tsx` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-1340-person-picker` |
| 2 | Failing tests written, test cases designed | yes | `person-picker.test.tsx` (module missing; then 1 failing for focus-opening and 1 for stale options) and `TS-0111` (2 failed: no combobox) observed failing before the change |
| 3 | Suite green, coverage held, docs updated | yes | `bash frontend/verify.sh` green; Playwright chromium suite against the branch build |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
