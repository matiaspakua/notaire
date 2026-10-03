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
| Issue | #1058 | open (implement in progress) |
| Use Case | CU22, CU59, CU24, CU25, CU50, CU23 | exists; UI entry notes updated |
| Related | audit-2026-09; E2E TS-0070/TS-0017/TS-0020; #1052 admin guard | referenced |
| Specification | `openspec/changes/fix-1058-suplencias-reportes-nav/` | Gate 1 validated |
| Branch | `cursor/fix-1058-suplencias-reportes-nav-69d3` | created from `origin/main` @ `8d8b72af` |
| Tasks | `tasks.md` | implement in progress |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Sidebar exposes Suplencias link to canonical route | `dashboard-nav.test.ts` + Playwright `nav-suplencias` (TS-0017/TS-0070/TS-0060) | covered |
| Sidebar exposes Reportes link to canonical route | `dashboard-nav.test.ts` + Playwright `nav-reportes` (TS-0020/TS-0070/TS-0060) | covered |
| Duplicate admin items page removed/redirected | `dashboard-nav.test.ts` redirects + redirect stub page | covered |
| Duplicate admin auditoria page removed/redirected | same | covered |
| E2E reaches Suplencias/Reportes via navigation UI | TS-0017, TS-0020 discovery, TS-0070 sidebar walk | covered |
| Role-appropriate visibility preserved for admin-only areas | `adminOnly` on administración; existing admin-access tests | covered |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| CU22 / CU59 / CU24 / CU25 / CU50 / CU23 UI entry notes | yes | (this PR) |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | yes | (this PR) |
| `CHANGELOG.md` | yes | (this PR) |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | artifacts in this folder; `validate-sdlc-plan.sh` PASS |
| 2 | Failing tests written, test cases designed | yes | unit red observed (i18n/redirects/duplicate pages), then green |
| 3 | Suite green, coverage held, docs updated | pending | frontend vitest/lint/typecheck green locally; heavy CI pending |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

Worker `gh` cannot add `in-progress` label / issue comments (GraphQL resource
not accessible). Coordinator owns label/project board moves and merge after
`check-heavy-ci.sh` exit 0.
