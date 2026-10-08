# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1250 | open → in progress |
| Use Case | CU13 – Gestionar historial de gestion; CU78 – Seguridad | exists |
| Specification | `docs/openspec/changes/fix-1250-history-writes-admin-only/` | Gate 1 draft |
| Branch | `fix/1250_history_writes_admin_only` | created from updated `main` |
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
| Employee rewrites history | `backend-api/src/test/java/com/licensis/notaire/integration/HistoryWriteAuthorizationIntegrationTest.java, Bruno rbac/08a` | passing |
| Employee deletes history | `backend-api/src/test/java/com/licensis/notaire/integration/HistoryWriteAuthorizationIntegrationTest.java, Bruno rbac/08b` | passing |
| Administrator corrects history | `backend-api/src/test/java/com/licensis/notaire/integration/HistoryWriteAuthorizationIntegrationTest.java` | passing |
| Employee reads and records history | `backend-api/src/test/java/com/licensis/notaire/integration/HistoryWriteAuthorizationIntegrationTest.java` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `backend-api/openapi/openapi.yaml` | yes | branch commit |
| `docs/200-architecture/206-security/API-AUTHENTICATION-GUIDE.md` | yes | branch commit |
| `contracts/api-reachability-allowlist.yaml` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-1250-history-writes-admin-only` |
| 2 | Failing tests written, test cases designed | yes | `HistoryWriteAuthorizationIntegrationTest` observed failing before the change |
| 3 | Suite green, coverage held, docs updated | partial | backend suite and Bruno green; oasdiff: no breaking changes (2 info: 403 added); run_pipeline.sh not run (no Docker) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` and the Testcontainers `pg-integration` tests need Docker, unavailable on the agent box.
