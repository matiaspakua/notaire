# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #655 | open → in progress |
| Use Case | CU69 – Gestión de Inmuebles; CU82 – Datos registrales del inmueble | exists |
| Specification | `docs/openspec/changes/fix-655-property-cadastral-designation-required/` | Gate 1 draft |
| Branch | `fix/655_property_cadastral_designation_required` | created from updated `main` |
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
| Create without designation | `backend-api/src/test/java/com/licensis/notaire/integration/PropertyRequestValidationIntegrationTest.java, Bruno properties/08` | passing |
| Update without designation | `backend-api/src/test/java/com/licensis/notaire/integration/PropertyRequestValidationIntegrationTest.java` | passing |
| Contract | `backend-api/src/test/java/com/licensis/notaire/integration/PropertyRequestValidationIntegrationTest.java` | passing |
| Form | `testing/e2e/tests/TS-0019-inmuebles-valuacion-workflow.spec.ts` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `backend-api/openapi/openapi.yaml` | yes | branch commit |
| `backend-api/openapi/accepted-breaking-changes.txt` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-655-property-cadastral-designation-required` |
| 2 | Failing tests written, test cases designed | yes | `PropertyRequestValidationIntegrationTest` and TS-0019 CU69-GW04 observed failing before the change |
| 3 | Suite green, coverage held, docs updated | partial | backend verify, Bruno and Playwright green; oasdiff passes with four accepted entries; stale-entry check OK; run_pipeline.sh not run (no Docker) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` and the Testcontainers `pg-integration` tests need Docker, unavailable on the agent box.
