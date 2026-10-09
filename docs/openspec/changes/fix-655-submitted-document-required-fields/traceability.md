# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #655 | open → in progress |
| Use Case | CU04 – Registrar documentación cliente; CU72 – Gestionar documentos presentados | exists |
| Specification | `docs/openspec/changes/fix-655-submitted-document-required-fields/` | Gate 1 draft |
| Branch | `fix/655_submitted_document_required_fields` | created from updated `main` |
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
| Missing type | `backend-api/src/test/java/com/licensis/notaire/integration/SubmittedDocumentRequestValidationIntegrationTest.java, Bruno submitted-documents/13` | passing |
| Missing procedure | `backend-api/src/test/java/com/licensis/notaire/integration/SubmittedDocumentRequestValidationIntegrationTest.java, Bruno submitted-documents/12` | passing |
| Empty body | `backend-api/src/test/java/com/licensis/notaire/integration/SubmittedDocumentRequestValidationIntegrationTest.java` | passing |
| Contract | `backend-api/src/test/java/com/licensis/notaire/integration/SubmittedDocumentRequestValidationIntegrationTest.java` | passing |
| Create form | `frontend/src/tests/unit/documento-presentado-form.test.ts, testing/e2e/tests/TS-0099-documento-tramite-link.spec.ts` | passing |
| Legacy document | `frontend/src/tests/unit/documento-presentado-form.test.ts, backend-api/src/test/java/com/licensis/notaire/integration/SubmittedDocumentPartialUpdateIntegrationTest.java` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `backend-api/openapi/openapi.yaml` | yes | branch commit |
| `backend-api/openapi/accepted-breaking-changes.txt` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-655-submitted-document-required-fields` |
| 2 | Failing tests written, test cases designed | yes | validation/contract tests, frontend unit test and Bruno reject requests observed failing before the change |
| 3 | Suite green, coverage held, docs updated | partial | backend verify, vitest, Bruno and Playwright green; oasdiff passes with two accepted entries; stale-entry check OK; run_pipeline.sh not run (no Docker) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` and the Testcontainers `pg-integration` tests need Docker, unavailable on the agent box.
