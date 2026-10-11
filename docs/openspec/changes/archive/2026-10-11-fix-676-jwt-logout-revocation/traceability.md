# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #676 | open (logout slice shipped; remainder Owner-gated) |
| Use Case | CU84 – Login | exists |
| Specification | `docs/openspec/changes/fix-676-jwt-logout-revocation/` | Gate 1 passed; archived |
| Living spec | `docs/openspec/specs/jwt-logout-revocation/spec.md` | synced |
| Branch | `fix/676_jwt_logout_revocation` | merged |
| Tasks | `tasks.md` | complete for logout slice |
| Commits | `af5409c0` | on `main` |
| Pull Request | [#1317](https://github.com/matiaspakua/notaire/pull/1317) | MERGED 2026-10-07 |
| CI run | PR #1317 checks | green at merge |
| Merge commit | `af5409c09c0e75a72692c85d00513fa937ef0542` | on `main` |
| Release / tag | CHANGELOG security entry | shipped |
| Smoke test | `JwtLogoutRevocationIntegrationTest` + `TokenRevocationServiceTest` | green on tip |

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
| `CHANGELOG.md` | yes | #1317 |
| `backend-api/openapi/openapi.yaml` | yes | #1317 |
| `docs/200-architecture/206-security/API-AUTHENTICATION-GUIDE.md` | yes | #1317 |
| `docs/200-architecture/205-data-model/Diccionario de Datos.md` | yes | #1317 |
| `docs/openspec/specs/jwt-logout-revocation/spec.md` | yes | archive hygiene |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-676-jwt-logout-revocation` |
| 2 | Failing tests written, test cases designed | yes | integration test written first and observed failing (200 after logout) |
| 3 | Suite green, coverage held, docs updated | yes | #1317 merge |
| 4 | CI green, review approved, no conflicts | yes | #1317 MERGED |
| 5 | Deployed, smoke test passed; slice archived | yes | living on `main`; archive PR; #676 remains open for non-slice remainder |

## Exceptions

Password-change revocation and refresh/lifetime policy stay on #676 and need Owner decisions before a new Gate 1 pack.
