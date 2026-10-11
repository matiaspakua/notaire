# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1361 | open → in progress |
| Use Case | RF-53 – Administrar tablas base; CU17 – Dar alta persona | exists |
| Specification | `docs/openspec/changes/feat-1361-identification-type-catalog/` | Gate 1 draft |
| Branch | `feat/1361_identification_type_catalog` | created from updated `main` |
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
| Admin lists, creates, edits and deletes a type | `identification-types-admin.test.tsx`, TS-0139 | passing |
| A type people use is not deleted | `IdentificationTypeCatalogIntegrationTest`, Bruno `identification-types/08`, TS-0139 | passing |
| Duplicate name is rejected | `IdentificationTypeCatalogIntegrationTest`, Bruno `identification-types/06`, TS-0139 | passing |
| Non-admin gets no catalog | TS-0139, TS-0094 | passing |
| Reachability without the 3 allowlist entries | `contracts/tests/test_api_reachability.py` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `backend-api/openapi/openapi.yaml` | yes | branch commit |
| `backend-api/api-test/COVERAGE.md` | yes | branch commit |
| `docs/300-development/303-testing/INVENTORY.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh feat-1361-identification-type-catalog` |
| 2 | Failing tests written, test cases designed | yes | JUnit 3/4, Vitest (page missing) and TS-0139 4/5 failing on the pre-change code |
| 3 | Suite green, coverage held, docs updated | yes | backend and frontend `verify.sh` green; Bruno and chromium suite (see PR) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the branch backend; Bruno ran against the branch jar on :8081.
