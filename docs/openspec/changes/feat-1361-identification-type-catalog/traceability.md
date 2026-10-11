# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
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
| Issue | #1361 | open (slice 2; slice 1 merged #1484) |
| Use Case | RF-53 – Administrar tablas base; CU78 – Security | exists |
| Specification | `docs/openspec/changes/feat-1361-identification-type-catalog/` | Gate 1 draft |
| Branch | `cursor/1361-identification-type-catalog-69d3` | created from updated `main` |
| Tasks | `tasks.md` | Gate 1 complete; impl pending |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Admin lists identification types | Vitest + TS-0127 | pending |
| Admin creates an identification type | Vitest + TS-0127 | pending |
| Admin edits an identification type | Vitest + TS-0127 | pending |
| Admin deletes an unused identification type | TS-0127 | pending |
| Delete in use shows a reason | Vitest (409 mock) | pending |
| Non-admin cannot open the catalog | TS-0127 | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | no | pending product PR |
| `CHANGELOG.md` | no | pending product PR (after #1518) |
| `contracts/api-reachability-allowlist.yaml` | no | pending product PR |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh feat-1361-identification-type-catalog` |
| 2 | Failing tests written, test cases designed | pending | blocked on #1518 heavy-CI serialize for product work |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

Product implementation and Playwright-heavy PR held until [#1518](https://github.com/matiaspakua/notaire/pull/1518) merges (CHANGELOG + heavy-CI serialize). Gate 1 OpenSpec prep proceeds now. `gh issue comment` may 403 for this agent — claim noted in fleet report only.
