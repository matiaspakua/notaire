# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #655 | open → in progress |
| Use Case | CU08 – Copias de testimonio; CU82 – Minuta de inscripción; CU43 – Reingreso de documentación | exists |
| Specification | `docs/openspec/changes/fix-655-required-ids-copy-draft-reentry/` | Gate 1 draft |
| Branch | `fix/655_required_ids_copy_draft_reentry` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | pushed |
| Pull Request | see PR | open |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Empty copy | `backend-api/src/test/java/com/licensis/notaire/integration/RequiredRequestIdsIntegrationTest.java, Bruno copies/08-create-empty-body` | passing |
| Copy update without print date | `backend-api/src/test/java/com/licensis/notaire/integration/RequiredRequestIdsIntegrationTest.java` | passing |
| Registration draft and re-entry without ids | `backend-api/src/test/java/com/licensis/notaire/integration/RequiredRequestIdsIntegrationTest.java, Bruno registration-drafts/06, managements/11` | passing |
| Copies dialog | `testing/e2e/tests/copias-required-fields.spec.ts` | passing |
| Contract | `backend-api/src/test/java/com/licensis/notaire/integration/RequiredRequestIdsIntegrationTest.java` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `backend-api/openapi/openapi.yaml` | yes | branch commit |
| `backend-api/openapi/accepted-breaking-changes.txt` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-655-required-ids-copy-draft-reentry` |
| 2 | Failing tests written, test cases designed | yes | `RequiredRequestIdsIntegrationTest` observed failing 8/8; copies Playwright spec failing first before the change |
| 3 | Suite green, coverage held, docs updated | partial | backend verify, Bruno and Playwright green; oasdiff reports no breaking change; stale-entry check OK; run_pipeline.sh not run (no Docker) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` and the Testcontainers `pg-integration` tests need Docker, unavailable on the agent box.
