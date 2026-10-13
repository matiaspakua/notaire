# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #596 | open → in progress |
| Use Case | CU74 – Performance and Caching Strategy | exists |
| Specification | `docs/openspec/changes/fix-596-pageable-parameter-object/` | Gate 1 draft |
| Branch | `fix/596_pageable_parameter_object` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | local, not pushed |
| Pull Request | — | pending (Owner approval) |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Optional paging parameters | `backend-api/src/test/java/com/licensis/notaire/integration/PagedEndpointsOpenApiParametersIntegrationTest.java` | passing |
| No pageable object | `backend-api/src/test/java/com/licensis/notaire/integration/PagedEndpointsOpenApiParametersIntegrationTest.java` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `backend-api/openapi/openapi.yaml` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-596-pageable-parameter-object` |
| 2 | Failing tests written, test cases designed | yes | `PagedEndpointsOpenApiParametersIntegrationTest` observed failing before the change |
| 3 | Suite green, coverage held, docs updated | partial | backend suite and Bruno green; oasdiff 0 ERR (5 WARN request-parameter-removed); run_pipeline.sh not run (no Docker) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` and the Testcontainers `pg-integration` tests need Docker, unavailable on the agent box.
