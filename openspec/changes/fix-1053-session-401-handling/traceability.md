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
| Issue | #1053 | open (move to in-progress at implement GO) |
| Use Case | CU84 – Login al sistema (`docs/100-business/102-use-cases/CU84 - Login.md`) | exists (update at Gate 3) |
| Related | #690 (session expiry E2E gap) | open — closed by this change’s E2E |
| Specification | `openspec/changes/fix-1053-session-401-handling/` | written |
| Branch | `cursor/fix-1053_session-401-handling-69d3` | created |
| Tasks | `tasks.md` | 0/N complete |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Authenticated API 401 clears session and redirects to login with expired flag | unit: api-client / session-handler; E2E TS-nnnn-session-expiry | pending |
| Login page shows session-expired message when expired=1 | unit/component: login page; E2E | pending |
| Non-401 API errors do not force logout | unit: api-client / query onError | pending |
| User can re-login after session expiry | E2E TS-nnnn-session-expiry | pending |
| Unauthenticated login 401 (bad credentials) does not loop redirect | unit + existing login E2E | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU84 - Login.md` | no | pending |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | no | pending |
| `CHANGELOG.md` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | artifacts in this folder; validate scripts |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
