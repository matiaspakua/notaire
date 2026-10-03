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
| Issue | #1052 | open (in-progress label ACL denied) |
| Use Case | CU78 – Security and Compliance; CU20/CU21 | updated (alt 3.2) |
| Specification | `openspec/changes/fix-1052-admin-route-guard/` | written |
| Branch | `cursor/fix-1052_admin-route-guard-69d3` | created |
| Tasks | `tasks.md` | implement done; CI pending |
| Commits | `1b229d86` fix(frontend): guard admin routes for non-admin users | pushed |
| Pull Request | #1137 | open (draft) |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Non-admin edge deny for administración path | `admin-access.test.ts` | pending |
| Admin edge allow for administración path | `admin-access.test.ts` | pending |
| Non-admin layout redirects with forbidden flag | unit/component + E2E TS-0094 | pending |
| Dashboard shows access-denied message | unit/component + E2E TS-0094 | pending |
| Non-admin cannot open admin routes (E2E) | `TS-0094-admin-route-guard.spec.ts` | pending |
| Admin still reaches administración | TS-0023 / TS-0094 | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU78 – Security and Compliance.md` | pending | — |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | pending | — |
| `CHANGELOG.md` | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | artifacts in this folder |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
