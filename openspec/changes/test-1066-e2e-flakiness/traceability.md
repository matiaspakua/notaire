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
| Issue | #1066 | open (implement in progress after #1145) |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | exists |
| Related | #1145 / #1054 (API error toasts — must merge first); audit-2026-09 | referenced |
| Specification | `openspec/changes/test-1066-e2e-flakiness/` | Gate 1 validated on branch |
| Branch | `cursor/fix-1066-e2e-flakiness-69d3` | created |
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
| Workflow editor arranges data instead of skipping | E2E: `TS-0021-workflow-editor-admin.spec.ts` | implemented |
| Workflow assignment arranges data instead of skipping | E2E: `TS-0022-workflow-assignment-admin.spec.ts` | implemented |
| Arrange failure fails the test | E2E / helper throw path in TS-0021/22 | implemented |
| Localization suite waits on locale signal | E2E: `TS-0040-l10n-language-switching-qa.spec.ts` | implemented |
| Icons QA suite waits on UI readiness | E2E: `TS-0043-icons-ux-qa.spec.ts` | implemented |
| Feature-gap skip cites an open issue | review/static: TS-0014/16/17/20 skip reasons | implemented (#1146) |
| CI retries are at most one | `playwright.config.ts` + CI Playwright job | implemented |
| Retry retains failure evidence | config: `trace` / screenshot / video settings | implemented |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | yes | — |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | yes | — |
| `docs/300-development/303-testing/TEST-PLAN.md` | yes | — |
| `CHANGELOG.md` | n/a (not user-visible) | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes (draft) | artifacts in this folder; validate via temp copy into `openspec/changes/` |
| 2 | Failing tests written, test cases designed | yes | `e2e-test-reliability.test.ts` red-then-green |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None. Implement deliberately deferred (no branch/PR/push) until #1145 (#1054)
merges — authorized by coordinator prep scope for Gate 1 scaffold only.
