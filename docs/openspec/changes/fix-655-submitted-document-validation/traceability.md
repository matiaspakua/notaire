# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #655 | open → in progress |
| Use Case | CU04 – Gestionar documentos presentados | exists |
| Specification | `docs/openspec/changes/fix-655-submitted-document-validation/` | Gate 1 draft |
| Branch | `fix/655_submitted_document_request_validation` | created from updated `main` |
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
| Invalid date on create | `backend-api/src/test/java/com/licensis/notaire/integration/SubmittedDocumentRequestValidationIntegrationTest.java` | passing |
| Unknown reference on create | `backend-api/src/test/java/com/licensis/notaire/integration/SubmittedDocumentRequestValidationIntegrationTest.java` | passing |
| Invalid input on update | `backend-api/src/test/java/com/licensis/notaire/integration/SubmittedDocumentRequestValidationIntegrationTest.java` | passing |
| Partial update of a stored document | `backend-api/src/test/java/com/licensis/notaire/integration/SubmittedDocumentRequestValidationIntegrationTest.java` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `backend-api/openapi/openapi.yaml` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-655-submitted-document-validation` |
| 2 | Failing tests written, test cases designed | yes | integration test written first and observed failing (201/200/500 instead of 400/404) |
| 3 | Suite green, coverage held, docs updated | partial | backend suite, Bruno and Playwright green; oasdiff no breaking changes; run_pipeline.sh not run (no Docker) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box.
