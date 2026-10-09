# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #567 | open → in progress |
| Use Case | CU01 – Preparar Presupuesto; CU03 – Lista documentos y certificados necesarios; CU09 – Registrar deudas documentos de Cliente; CU13 – Ver historial de gestión; CU42 – Informar próximos vencimientos | exists |
| Specification | `docs/openspec/changes/fix-567-inhouse-report-pdfs/` | Gate 1 draft |
| Branch | `fix/567_inhouse_report_pdfs` | created from updated `main` |
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
| Budget report | `backend-api/src/test/java/com/licensis/notaire/integration/InHouseReportPdfIntegrationTest.java, Bruno report-pdfs/06` | passing |
| Budget report with properties | `backend-api/src/test/java/com/licensis/notaire/integration/InHouseReportPdfIntegrationTest.java, Bruno report-pdfs/07` | passing |
| Procedure documents report | `backend-api/src/test/java/com/licensis/notaire/integration/InHouseReportPdfIntegrationTest.java, Bruno report-pdfs/03` | passing |
| Management history report | `backend-api/src/test/java/com/licensis/notaire/integration/InHouseReportPdfIntegrationTest.java, Bruno report-pdfs/10` | passing |
| Submitted document expiry report | `backend-api/src/test/java/com/licensis/notaire/integration/InHouseReportPdfIntegrationTest.java` | passing |
| Document debt report | `backend-api/src/test/java/com/licensis/notaire/integration/InHouseReportPdfIntegrationTest.java, Bruno report-pdfs/12, frontend/src/tests/unit/useReportes-contract.test.ts` | passing |
| Missing aggregate | `backend-api/src/test/java/com/licensis/notaire/integration/InHouseReportPdfIntegrationTest.java, Bruno report-pdfs/03a, 09, 11, 13` | passing |
| Readable multi-page PDF | `backend-api/src/test/java/com/licensis/notaire/adapter/out/pdf/PdfBoxReportRendererTest.java` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `backend-api/openapi/openapi.yaml` | yes | branch commit |
| `docs/200-architecture/201-SAD/sad.md` | yes | branch commit |
| `docs/200-architecture/203-design/REST-API-REFERENCE.md` | yes | branch commit |
| `AGENTS.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-567-inhouse-report-pdfs` |
| 2 | Failing tests written, test cases designed | yes | `InHouseReportPdfIntegrationTest`, `PdfBoxReportRendererTest` and `useReportes-contract.test.ts` observed failing before the change |
| 3 | Suite green, coverage held, docs updated | partial | backend suite, frontend unit tests, Bruno and Playwright green; oasdiff: no breaking changes (18 info: 400/404/500 added); run_pipeline.sh not run (no Docker) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` and the Testcontainers `pg-integration` tests need Docker, unavailable on the agent box.
