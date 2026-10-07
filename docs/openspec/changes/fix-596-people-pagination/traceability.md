# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #596 | open → in progress |
| Use Case | CU18 – Dar Alta Cliente; CU54 – Modificar Persona; CU61 – Buscar persona | exists |
| Specification | `docs/openspec/changes/fix-596-people-pagination/` | Gate 1 draft |
| Branch | `fix/596_people_pagination` | created from updated `main` |
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
| Requested page size | `PeoplePaginationIntegrationTest, Bruno people/02-list` | passing |
| Default page | `PeoplePaginationIntegrationTest` | passing |
| People screen lists people | `use-personas-pagination.test.tsx, Playwright TS-0015` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `backend-api/openapi/openapi.yaml` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-596-people-pagination` |
| 2 | Failing tests written, test cases designed | yes | `PeoplePaginationIntegrationTest` and the Vitest hook test observed failing before the change |
| 3 | Suite green, coverage held, docs updated | partial | backend full suite, Vitest, tsc, lint, Bruno people and Playwright green locally; run_pipeline.sh not run (no Docker) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box. The non-required OpenAPI breaking-diff check flags the intended contract change.
