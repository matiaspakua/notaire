# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
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
| Issue | #2407 | done |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/feat-2407-production-line-diagram/` | written |
| Branch | `feat/2407_production_line_diagram` | created |
| Tasks | `tasks.md` | <!-- n/N complete --> |
| Commits | e7ae8692, 117697a4, 8b01e100, 2aab791b, b95c23a1, a207573c, 6753a2c3, 71d26cc1 | done |
| Pull Request | #<!-- number --> | pending |
| CI run | <!-- workflow run URL --> | pending |
| Merge commit | <!-- SHA --> | pending |
| Release / tag | <!-- vX.Y.Z --> | pending |
| Smoke test | <!-- environment + result --> | pending |

## Requirement coverage

<!-- Every `#### Scenario:` in the delta spec is an Acceptance Criterion and must
     appear here with the test that proves it. Requirement coverage is not code
     coverage: a green suite with an unmapped scenario means the criterion is
     unverified. -->

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| One station per stage in line order | `tests/line-model.test.ts`, `tests/e2e/dashboard.spec.ts` | passing |
| The running station holds the token | `tests/line-model.test.ts`, `tests/e2e/dashboard.spec.ts` | passing |
| A failed run marks its station | `tests/line-model.test.ts` | passing |
| Small screens get a vertical line | `tests/e2e/dashboard.spec.ts` | passing |
| Kinds follow the line data | `tests/line-model.test.ts` | passing |
| A skipped gate is not evaluable | `tests/line-model.test.ts` | passing |
| A gate without evidence says so | `tests/line-model.test.ts` | passing |
| Loop between pre-push and red | `tests/e2e/dashboard.spec.ts` | passing |
| Step rows sum into their station | `tests/line-model.test.ts` | passing |
| No telemetry | `tests/line-model.test.ts` | passing |
| Progress of a running intent | `tests/line-model.test.ts` | passing |
| No intent attached | `tests/e2e/dashboard.spec.ts` | passing |
| Keyboard selection | `tests/e2e/dashboard.spec.ts` | passing |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| <!-- path --> | <!-- yes/no --> | <!-- SHA --> |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | <!-- --> | <!-- --> |
| 2 | Failing tests written, test cases designed | <!-- --> | <!-- --> |
| 3 | Suite green, coverage held, docs updated | <!-- --> | <!-- --> |
| 4 | CI green, review approved, no conflicts | <!-- --> | <!-- --> |
| 5 | Deployed, smoke test passed, Issue closed | <!-- --> | <!-- --> |

## Exceptions

None. The workflow version in task 4.6 is a configuration change done by a worker (Junie) in the same branch.
