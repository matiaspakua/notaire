# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1340 | open → in progress |
| Use Case | CU06 – Firmar escritura; CU07 – Generar testimonio; CU82 – Minuta de inscripción | exists |
| Specification | `docs/openspec/changes/feat-1340-deed-status-filter/` | Gate 1 draft |
| Branch | `feat/1340_deed_status_filter` | created from updated `main` |
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
| Signed deeds only | `DeedStatusFilterIntegrationTest`, Bruno `deeds/02-list-by-status.yml` | passing |
| Unknown status | `DeedStatusFilterIntegrationTest`, Bruno `deeds/03-list-invalid-status.yml` | passing |
| No filter | `DeedStatusFilterIntegrationTest`, `PagedEndpointsOpenApiParametersIntegrationTest` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `backend-api/openapi/openapi.yaml` | yes | branch commit |
| `backend-api/api-test/COVERAGE.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh feat-1340-deed-status-filter` |
| 2 | Failing tests written, test cases designed | yes | `DeedStatusFilterIntegrationTest` 4/5 failing before the change |
| 3 | Suite green, coverage held, docs updated | yes | `bash backend-api/verify.sh`; Bruno 360/360 |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Bruno ran against the branch jar on :8081 with native PostgreSQL 17.
