# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #655 | open → in progress |
| Use Case | CU07 – Testimonios; CU53 – Trámites; CU26 – Tipos de trámite | exists |
| Specification | `docs/openspec/changes/fix-655-put-rejects-incomplete-bodies/` | Gate 1 draft |
| Branch | `fix/655_put_rejects_incomplete_bodies` | stacked on #1371 / #1370 |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | pushed |
| Pull Request | #1374 | open |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Empty testimony update | `backend-api/src/test/java/com/licensis/notaire/integration/IncompletePutBodiesIntegrationTest.java`, Bruno `testimonies/04a` + `05` | passing |
| Incomplete testimony update | `IncompletePutBodiesIntegrationTest.testimonyIncompleteBodyIsRejected`, `RequiredFieldsTest` | passing |
| Procedure update without type | `IncompletePutBodiesIntegrationTest.procedureWithoutTypeIsRejected`, Bruno `procedures/04a` + `05` | passing |
| Workflow assignment | `IncompletePutBodiesIntegrationTest.workflowAssignment*`, Bruno `procedure-types/04a`–`04b`, `frontend/src/hooks/useTiposTramite.test.tsx` | passing |
| Contract | `IncompletePutBodiesIntegrationTest.contractDocumentsRequiredFields` | passing |
| Testimony update without version | `StaleOrMissingVersionIntegrationTest.testimonyMissingVersionIsRejected`, Bruno `testimonies/04c`, `04a` | passing |
| Stale testimony version | `StaleOrMissingVersionIntegrationTest.testimonyStaleVersionIsConflict` / `testimonyFutureVersionIsConflict`, Bruno `testimonies/04b` | passing |
| Stale version on another versioned update | `StaleOrMissingVersionIntegrationTest.catalogStaleVersionIsConflict` / `testimonyMovementStaleVersionIsConflict` / `versionedUpdatesDocument409`, `GlobalExceptionHandlerOptimisticLockTest`, `ErrorResponsesTest`, Bruno `testimony-movements/04b` | passing |
| UI sends the version it read | `testing/e2e/tests/folio-type-edit-version.spec.ts` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `backend-api/openapi/openapi.yaml` | yes | branch commit |
| `backend-api/openapi/accepted-breaking-changes.txt` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-655-put-rejects-incomplete-bodies` |
| 2 | Failing tests written, test cases designed | yes | `IncompletePutBodiesIntegrationTest` observed failing 9/10 before the change |
| 3 | Suite green, coverage held, docs updated | partial | backend verify, Bruno and Playwright green; oasdiff clean with the six accepted entries; stale-entry check OK; run_pipeline.sh not run (no Docker) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` and the Testcontainers `pg-integration` tests need Docker, unavailable on the agent box.
