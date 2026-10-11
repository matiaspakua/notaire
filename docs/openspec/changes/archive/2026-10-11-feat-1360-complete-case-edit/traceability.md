# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1360 | open → in progress |
| Use Case | CU02 – Crear gestión; CU53 – Modificar gestión; CU22/CU59 – Suplencia; RF-22 | exists |
| Specification | `docs/openspec/changes/feat-1360-complete-case-edit/` | Gate 1 draft |
| Branch | `feat/1360_complete_case_edit` | created from updated `main` |
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
| Edit dialog is prefilled, status read-only | TS-0126 | passing |
| Notary change persists, notes kept | TS-0126, `ManagementCompleteCaseReadIntegrationTest`, Bruno `procedure-folders/05c-05d` | passing |
| Substitution redirect on edit shows a toast | TS-0126 | passing |
| Case values read in the PUT's shape | `ManagementCompleteCaseReadIntegrationTest`, Bruno `procedure-folders/05b`, `useManagements.complete-case.test.tsx` | passing |
| Unknown management is 404 | `ManagementCompleteCaseReadIntegrationTest` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `backend-api/openapi/openapi.yaml` | yes | branch commit |
| `backend-api/api-test/COVERAGE.md` | yes | branch commit |
| `contracts/api-reachability-allowlist.yaml` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh feat-1360-complete-case-edit` |
| 2 | Failing tests written, test cases designed | yes | JUnit 3/3 (405), Vitest 3/3 and TS-0126 3/3 failing on the pre-change code |
| 3 | Suite green, coverage held, docs updated | yes | backend and frontend `verify.sh` green; Bruno 364/364 and Playwright (see PR) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the branch backend on :8080; Bruno ran against the branch jar on :8081.
