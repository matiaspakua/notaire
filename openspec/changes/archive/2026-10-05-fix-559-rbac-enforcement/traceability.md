# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #559 | open → in progress |
| Use Case | CU78 – Security, Privacy and Compliance | exists |
| Related | #1052 (frontend admin guard), #676 (JWT lifecycle), #1242 (assessment) | referenced |
| Specification | `openspec/changes/fix-559-rbac-enforcement/` | Gate 1 draft |
| Branch | `fix/559_rbac_enforcement` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | in progress |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Administrator-capable types | `UserAuthorityResolverTest` | pending |
| Other types | `UserAuthorityResolverTest` | pending |
| Removed or inactive user | `RbacIntegrationTest` | pending |
| Employee cannot manage users | `RbacIntegrationTest` | pending |
| Employee cannot read roles or the audit log | `RbacIntegrationTest` | pending |
| Login and logout stay public | `JwtAuthIntegrationTest` | pending |
| Employee cannot change a catalog | `RbacIntegrationTest` | pending |
| Employee can read a catalog | `RbacIntegrationTest` | pending |
| Administrator keeps full access | `RbacIntegrationTest` | pending |
| Employee opens the audit screen | `testing/e2e/tests/TS-0094-admin-route-guard.spec.ts` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/202-ADR/ADR-008-security-authentication.md` | pending | — |
| `docs/200-architecture/206-security/API-AUTHENTICATION-GUIDE.md` | pending | — |
| `CHANGELOG.md` | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | `bash scripts/validate-sdlc-plan.sh` |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
