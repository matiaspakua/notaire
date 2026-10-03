# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1051 | open (implement in progress) |
| Use Case | CU78 – Security and Compliance; CU84 – Login al sistema | exists |
| Related | #1052 (UX role cookies); #1053 (session 401); #676 OPEN (refresh/revocation — out of scope); #691/TS-0044 CSRF posture; audit-2026-09 | referenced |
| Specification | `openspec/changes/fix-1051-httponly-jwt-csp/` | Gate 1 ready |
| Branch | `cursor/fix-1051-httponly-jwt-csp-69d3` | active |
| Tasks | `tasks.md` | Gate 1 planning complete; implement pending |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Login sets HttpOnly Secure SameSite cookie | `JwtAuthIntegrationTest#shouldSetHttpOnlyAuthCookieOnLogin` | covered |
| JWT is not stored in localStorage after login | `auth-store` / login-page unit + TS-0002 / TS-0044 | covered |
| Browser API call authenticates via proxy and cookie | api-client credentials unit + TS-0044 | covered |
| API accepts cookie without Bearer | `JwtAuthenticationFilterTest` + integration | covered |
| API still accepts Bearer for non-browser clients | `JwtAuthIntegrationTest` Bearer cases | covered |
| Logout clears auth cookie | integration logout + TS-0002 | covered |
| Production CSP has no unsafe-eval | `csp.test.ts` | covered |
| Production CSP script-src is nonce-based | `csp.test.ts` | covered |
| App remains functional under nonce CSP | Playwright login/logout (heavy CI) | pending CI |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU78 – Security and Compliance.md` | yes | follow-up |
| `docs/100-business/102-use-cases/CU84 - Login.md` | yes | follow-up |
| `docs/200-architecture/206-security/API-AUTHENTICATION-GUIDE.md` | yes | follow-up |
| `docs/200-architecture/201-SAD/sad.md` | yes | follow-up |
| `CHANGELOG.md` | yes | follow-up |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes (draft) | artifacts in this folder; validate via temp copy into `openspec/changes/` |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None. Implement deliberately deferred (no branch/PR/push) until **#1048 → #1047 →
\#1044** clear and no Playwright-heavy PR is in flight — authorized by coordinator
prep-only scope while PR #1150 CI finishes.
