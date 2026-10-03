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
| Issue | #1056 | open → implement in progress |
| Use Case | CU84 – Login al sistema | exists |
| Related | #1052 shipped; #1051 on main (HttpOnly JWT + CSP nonce — do not regress); #1043/#1045 merged (`0607cf0a`) | referenced |
| Specification | `openspec/changes/chore-1056-middleware-to-proxy/` | Gate 1 validated |
| Branch | `cursor/chore-1056-middleware-to-proxy-69d3` | created from `origin/main` @ `0607cf0a` |
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
| Codemod yields proxy.ts with export proxy | `proxy-convention.test.ts` | covered |
| Deprecated middleware.ts is removed | `proxy-convention.test.ts` | covered |
| Unauthenticated protected route redirects to login | `edge-proxy.test.ts` + Playwright auth | unit green; E2E via CI |
| Authenticated login path redirects to dashboard | `edge-proxy.test.ts` + Playwright TS-0002 | unit green; E2E via CI |
| Non-admin is denied admin routes at the edge | `admin-access.test.ts` + `edge-proxy.test.ts` + TS-0094 | unit green; E2E via CI |
| API proxy paths skip edge auth redirects | `edge-proxy.test.ts` | covered |
| next build has no middleware deprecation warning | local `npm run build` post-codemod | covered |
| Auth E2E suite stays green | Playwright auth suite / heavy CI | pending CI |
| HttpOnly cookie auth path is not regressed | edge preserves `/api/` skip + CSP nonce; CI Playwright | unit preserved; E2E via CI |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU84 - Login.md` | yes | (this PR) |
| `docs/100-business/102-use-cases/CU78 – Security and Compliance.md` | yes | (this PR) |
| `docs/300-development/303-testing/FRONTEND-TESTING-GUIDE.md` | yes | (this PR) |
| Frontend comments citing edge middleware for route guards | yes | (this PR) |
| `CHANGELOG.md` | yes | (this PR) |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh chore-1056-middleware-to-proxy` |
| 2 | Failing tests written, test cases designed | yes | proxy-convention failed red on pre-change tree; deprecation in pre-build log |
| 3 | Suite green, coverage held, docs updated | yes (frontend) | `npm test` 340; lint; typecheck; build without middleware deprecation |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None. Implement deliberately deferred (no product branch/PR/push) until the
coordinator clears the **#1043/#1045** queue (or promotes earlier), prefers
**#1051** first when the edge file gains CSP nonce, and no Playwright-heavy PR
is in flight — authorized by Gate 1–only assignment scope.
