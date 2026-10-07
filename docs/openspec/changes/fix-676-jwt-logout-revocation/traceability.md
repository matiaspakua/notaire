# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #676 | open → in progress |
| Use Case | CU84 – Login | exists |
| Specification | `docs/openspec/changes/fix-676-jwt-logout-revocation/` | Gate 1 draft |
| Branch | `fix/676_jwt_logout_revocation` | created from updated `main` |
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
| Token rejected after logout | `backend-api/src/test/java/com/licensis/notaire/integration/JwtLogoutRevocationIntegrationTest.java` | passing |
| Other session unaffected | `backend-api/src/test/java/com/licensis/notaire/integration/JwtLogoutRevocationIntegrationTest.java` | passing |
| Logout without a valid token | `backend-api/src/test/java/com/licensis/notaire/integration/JwtLogoutRevocationIntegrationTest.java` | passing |
| Expired revocations purged | `backend-api/src/test/java/com/licensis/notaire/unit/TokenRevocationServiceTest.java` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `backend-api/openapi/openapi.yaml` | yes | branch commit |
| `docs/200-architecture/206-security/API-AUTHENTICATION-GUIDE.md` | yes | branch commit |
| `docs/200-architecture/205-data-model/Diccionario de Datos.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-676-jwt-logout-revocation` |
| 2 | Failing tests written, test cases designed | yes | integration test written first and observed failing (200 after logout) |
| 3 | Suite green, coverage held, docs updated | partial | backend suite, docs, contracts and workspace guards green; run_pipeline.sh not run (no Docker) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box.
