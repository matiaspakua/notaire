# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1053 | open (implement in progress; in-progress label ACL denied) |
| Use Case | CU84 – Login al sistema (`docs/100-business/102-use-cases/CU84 - Login.md`) | updated (alt flow 3e) |
| Related | #690 (session expiry E2E gap) | closed by TS-0093 when #1053 merges |
| Specification | `openspec/changes/fix-1053-session-401-handling/` | written |
| Branch | `cursor/fix-1053_session-401-handling-69d3` | created |
| Tasks | `tasks.md` | Gate 2–3 implementation in progress |
| Commits | `520dff65` fix(frontend): handle expired sessions on authenticated 401 | pushed pending |
| Pull Request | #1131 | open (implement) |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Authenticated API 401 clears session and redirects to login with expired flag | `session-expiry.test.ts`, `api-client.test.ts`; E2E `TS-0093` | unit green; E2E pending CI |
| Login page shows session-expired message when expired=1 | `login-page.test.tsx`; E2E `TS-0093` | unit green; E2E pending CI |
| Non-401 API errors do not force logout | `session-expiry.test.ts`, `api-client.test.ts` | green |
| User can re-login after session expiry | E2E `TS-0093` | pending CI |
| Unauthenticated login 401 (bad credentials) does not loop redirect | `session-expiry.test.ts`, `api-client.test.ts`; TS-0001 | unit green |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU84 - Login.md` | yes | pending SHA |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | yes | pending SHA |
| `CHANGELOG.md` | yes | pending SHA |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | artifacts in this folder; validate scripts |
| 2 | Failing tests written, test cases designed | yes | observed missing-module fail; then 280 vitest green |
| 3 | Suite green, coverage held, docs updated | partial | docs done; Playwright/CI pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
