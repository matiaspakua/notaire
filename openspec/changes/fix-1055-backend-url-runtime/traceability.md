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
| Issue | #1055 | open — implement in progress after #1056 |
| Use Case | CU78 – Security, Privacy and Compliance | exists |
| Related | #1051 CLOSED/merged (`c2c34de8`) HttpOnly JWT+CSP — do not regress; #1056 merged (`d1c5c5f2` / PR #1167) middleware→proxy; audit-2026-09 | referenced |
| Specification | `openspec/changes/fix-1055-backend-url-runtime/` | Gate 1 validated |
| Branch | `cursor/fix-1055-backend-url-runtime-69d3` | created from `origin/main` @ `d1c5c5f2` |
| Tasks | `tasks.md` | implement in progress |
| Commits | `c8f9bbd3d0bef810598dc6e14443d7d6c9cc0c02` | committed |
| Pull Request | [#1168](https://github.com/matiaspakua/notaire/pull/1168) | draft open |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Proxy uses runtime BACKEND_URL | `frontend/src/tests/unit/backend-proxy.test.ts` | covered |
| Missing BACKEND_URL fails safely | `backend-proxy.test.ts` (503, no NEXT_PUBLIC fallback) | covered |
| One image works across environments | `backend-proxy.test.ts` env swap + compose/Dockerfile runtime `BACKEND_URL` | covered |
| Login page has no Backend URL line | `login-page.test.tsx` + Playwright TS-0001 | covered |
| Login HTML omits internal Docker API host | Playwright TS-0001 assertion | covered |
| Cookie header is forwarded upstream | `backend-proxy.test.ts` | covered |
| Set-Cookie from login/logout is relayed to the browser | `backend-proxy.test.ts` + Playwright auth (CI) | covered (unit); E2E via heavy CI |
| Auth E2E login path stays green | Playwright auth suite / heavy CI | pending CI |
| API paths are not redirected to login by the edge interceptor | `edge-proxy.test.ts` + `proxy-convention.test.ts` | covered |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU78 – Security and Compliance.md` | yes | pending SHA |
| Frontend / deployment docs for `BACKEND_URL` vs `NEXT_PUBLIC_API_URL` / rewrites | yes (`frontend/README.md`, deployment README, RELEASE.md, SAD) | pending SHA |
| `docs/200-architecture/209-deployment/README.md` | yes | pending SHA |
| `CHANGELOG.md` | yes | pending SHA |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh fix-1055-backend-url-runtime` |
| 2 | Failing tests written, test cases designed | yes | observed red on proxy helper / login leak / rewrite bake, then green |
| 3 | Suite green, coverage held, docs updated | pending | frontend unit+lint+typecheck+build green; heavy CI pending |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | coordinator merge after heavy-CI |

## Exceptions

None. Label `in-progress` ACL returned 403 for integration token (expected).
Merge deferred to coordinator after `bash scripts/check-heavy-ci.sh` exit 0.
